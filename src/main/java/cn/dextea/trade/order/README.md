# order 模块（订单域 · 核心域）

`order` 是本系统的**核心域**，承载交易主链路：下单前校验与计价（pre-build）、
创建订单、支付对接、订单状态流转。所有与「交易」相关的业务规则都收敛在此。

## 目录结构

```
order/
├── interfaces/               接口层（对外暴露）
│   ├── controller/OrderController.java   REST 入口：创建 / 预构建 / 查询订单
│   ├── assembler/OrderAssembler.java     DTO ↔ 命令/领域对象 装配
│   └── dto/                             请求/响应 DTO（14 个，含不可用项明细）
├── application/              应用层（用例编排）
│   ├── OrderCommandService / OrderQueryService   应用服务接口
│   ├── impl/                                         实现：幂等、落库、调支付、缓存
│   └── command/                                     命令对象（CreateOrder / PreBuildOrder / OrderProduct）
├── domain/                  领域层（业务内核，最稳定）
│   ├── enums/               DiningMethod / TradeStatus / MakingStatus / OrderEvent
│   ├── exception/OrderErrorCode.java  模块级错误码
│   ├── model/               实体与值对象（Order / OrderItem / PreBuild* / PricedOrderItem / 不可用明细 等）
│   ├── service/             领域服务：OrderPlacementDomainService（计价校验）、OrderStatusDomainService、OrderStatusMachine（状态机）
│   ├── port/               防腐端口（接口）：ProductCatalogPort / StorePort / CustomerPort / PaymentClientPort / OrderRepository / OrderIdGeneratorPort / OrderLockPort
│   └── util/SkuIdParser.java   skuId 解析工具（从编码中拆出 商品/选项/客制化项）
└── infrastructure/          基础设施层（技术落地）
    ├── adapter/             实现 domain/port（7 个 *Adapter：catalog/store/customer/payment/lock/id/支付同步）
    └── persistence/         MyBatis Mapper + OrderRepositoryImpl（含 CAS 状态更新、状态日志）
```

## 关键业务流程

### 1. 预构建（pre-build，只读、不落库）
`OrderController.preBuildOrder` → `OrderCommandService.preBuildOrder` →
`OrderPlacementDomainService.preBuild`：

1. 校验门店、顾客是否可用（经 `StorePort` / `CustomerPort`）。
2. 用 `SkuIdParser` 解析 `skuId`，得到商品/选项/客制化项 ID。
3. 经 `ProductCatalogPort` **批量**拉取商品、封面、门店状态、客制化、选项快照。
4. 逐项校验可用性：
   - 商品级剔除（全局下架 / 门店售罄）→ `UnavailableProduct`
   - 选项级剔除（客制化项未激活 / 选项禁用 / **跨绑定异常**）→ `UnavailableCustomization`
5. 有效项计价，产出 `PreBuildResult`（总价、总数量、明细、不可用清单）。

> 设计为**只读、不落库、不占幂等键**：前端可反复调用以刷新购物车，存在不可用项时
> 返回清单而不创建订单，便于用户修正后重试。

### 2. 创建订单（create，落库 + 调支付）
`OrderCommandService.createOrder`：

1. 拦截不支持的支付方式（微信支付暂未实现，抛 `PayErrorCode`）。
2. **幂等**：先查 Redis（`dextea:order:idem:<key>`，TTL 24h）；命中直接返回缓存结果。
3. 复用 `preBuild` 校验数据合法性与计价；门店/顾客不可用直接拒绝；存在不可用项返回 `preBuild` 结果（不创建订单、不占幂等键）。
4. **落库**：`OrderRepository.save`；MySQL 唯一索引对幂等键兜底——若 Redis 校验过期但库已存在，捕获 `DuplicateKeyException` 查回已有订单复用。
5. **支付**：支付宝订单经 `PaymentClientPort.createPayment` 创建交易并回填 `trade_no`（需顾客已绑定支付宝 `alipayOpenId`）。
6. 缓存首次创建结果到 Redis，供后续相同幂等键请求快速返回。

### 3. 订单状态机
`OrderStatusMachine` 用「当前状态 + 事件 → 目标状态」白名单描述合法流转：

- `待支付` --(支付/支付完成/关闭)--> `已支付` / `已结算` / `已关闭`
- `已支付` / `已结算` --(退款)--> `已退款`

不在白名单的组合视为非法流转。状态更新通过 `OrderRepository.updateStatusCas`
以**乐观锁（version + 期望状态）**保证并发安全，并写 `OrderStatusLog` 流水。

## 防腐端口（port）清单

| 端口 | 抽象内容 | 实现（`infrastructure/adapter`） |
|------|----------|----------------------------------|
| `ProductCatalogPort` | 只读商品/客制化/封面快照 | `ProductCatalogAdapter` → catalog 域 `CatalogQueryService` |
| `StorePort` / `CustomerPort` | 只读门店/顾客 | `*Adapter` → catalog 域 |
| `PaymentClientPort` | 创建支付交易 | `PaymentClientAdapter` → pay 应用 `PaymentService` |
| `OrderRepository` | 订单持久化（含 CAS） | `OrderRepositoryImpl` + Mapper |
| `OrderIdGeneratorPort` | 分布式订单号 | `OrderIdGeneratorAdapter` → CosId |
| `OrderLockPort` | 分布式锁 | `OrderLockAdapter` → Redis |
| （支付结果回写） | 支付成功/关闭同步 | `OrderPaymentSyncAdapter` 实现 pay 域的 `PaymentResultSyncPort` |

> 设计核心：**order 领域层完全不 import 任何基础设施或第三方 SDK**，所有外部能力
> 都通过 `port` 接口抽象。要替换支付渠道、ID 生成器或 DB 框架，只需换 `adapter`，
> 不动领域代码。

## 扩展指引

- 新增订单字段：在 `domain/model/Order`（及 `OrderItem`）加属性 → 同步 DTO 与
  `OrderAssembler` → `persistence` Mapper 的 SQL。
- 新增状态流转：在 `enums/OrderEventEnum` + `OrderStatusMachine.RULES` 注册，
  并确认 `PaymentResultSyncPort` 适配器映射正确。
- 接入新支付渠道：实现 order 侧 `PaymentClientAdapter` 与 pay 侧 `PaymentPort`。
