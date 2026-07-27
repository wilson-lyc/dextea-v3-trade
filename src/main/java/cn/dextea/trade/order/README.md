# order 模块（订单域 · 核心域）

`order` 是本系统的**核心域**，承载交易主链路：下单前校验与计价（pre-build）、
创建订单、支付对接、订单状态流转。所有与「交易」相关的业务规则都收敛在此。

> 重构后，`order` 包已吸收原 `catalog`（商品/客制化/门店/顾客支撑域）与
> `infrastructure.acl`（防腐层过渡产物）的全部内容：商品/客制化/门店/顾客数据以
> **只读快照值对象** 形式由领域层经 `gateway` 接口消费；其数据库结构（含图库
> `product_image` / `gallery` 的两段 join）被严格隔离在基础设施层。

## 四层目录结构

```
order/
├── api/                        接口层（对外暴露，按读写职责拆分）
│   ├── controller/
│   │   ├── OrderCommandController.java   写操作：create / pre-build
│   │   └── OrderQueryController.java     读操作：列表 / 详情
│   ├── dto/request/            入参 DTO（CreateOrderRequest / PreBuildOrderRequest 等）
│   ├── dto/response/           出参 DTO（OrderDetailResponse / OrderSummary / PreBuildOrderResponse 等）
│   └── assembler/
│       └── OrderApiAssembler.java        Request→Command、领域/应用 DTO→Response
├── application/                应用层（用例编排）
│   ├── service/
│   │   ├── OrderApplicationService.java  应用服务接口（写）
│   │   ├── OrderQueryService.java        查询服务接口（读，绕过领域）
│   │   └── impl/                         幂等、落库、调支付、缓存
│   ├── command/                命令对象（CreateOrderCommand / PreBuildOrderCommand / OrderProductItem）
│   ├── dto/                    应用层出参 DTO（OrderDetailDTO / OrderItemDTO / OrderSummaryDTO / StoreInfoDTO / OrderCreateResult）
│   ├── assembler/
│   │   └── OrderAssembler.java          Command→Domain、Domain→应用 DTO
│   └── facade/
│       └── ExternalDataFacade.java      聚合 Product/Customization/Store/Customer 四网关调用
├── domain/                     领域层（业务内核，最稳定，零外部依赖）
│   ├── model/aggregate/Order.java                聚合根
│   ├── model/entity/OrderItem.java               实体（含 coverId 封面标识）
│   ├── model/valueobject/                        只读快照值对象 + 值对象
│   │   ├── Product / ProductStoreStatus          商品及门店可售快照
│   │   ├── Customization / CustomizationOption / CustomizationOptionStoreStatus
│   │   ├── Store / Customer                       门店 / 顾客快照
│   │   └── Money / OrderStatus / Address          金额 / 状态 / 地址值对象
│   ├── enums/                   7 个状态枚举（原 catalog）+ 既有交易枚举
│   ├── exception/OrderErrorCode.java              模块级错误码
│   ├── service/                领域服务：OrderPlacementDomainService（计价校验）、OrderStatusDomainService、OrderStatusMachine（状态机）
│   ├── repository/OrderRepository.java            订单仓储接口
│   ├── gateway/                网关接口（原 port，防腐抽象）
│   │   ├── ProductGateway / CustomizationGateway / StoreGateway / CustomerGateway   外部域只读快照
│   │   ├── PaymentClientGateway                  创建支付交易
│   │   ├── OrderIdGeneratorGateway / OrderLockGateway
│   │   └── ProductCover                          productId + coverId + coverUrl 跨层载体
│   └── util/SkuIdParser.java      skuId 解析工具
└── infrastructure/             基础设施层（技术落地）
    ├── gateway/
    │   ├── impl/               网关实现（ACL 落地）
    │   │   ├── ProductGatewayImpl / CustomizationGatewayImpl / StoreGatewayImpl / CustomerGatewayImpl   委托 CatalogMapper
    │   │   ├── PaymentClientAdapter              调 pay 应用 PaymentService
    │   │   ├── OrderPaymentSyncAdapter           实现 pay 域 PaymentResultSyncGateway（支付结果回写）
    │   │   ├── OrderIdGeneratorAdapter           分布式订单号（CosId）
    │   │   └── OrderLockAdapter                  分布式锁（Redis）
    │   ├── mapper/              CatalogMapper（商品/客制化/门店/顾客表注解 SQL）
    │   ├── po/                  外部表 PO（ProductPO / StorePO / CustomerPO / CustomizationPO / GalleryPO / ProductImagePO 等）
    │   └── translator/         PO→值对象映射（ProductTranslator / CustomizationTranslator / StoreTranslator / CustomerTranslator）
    ├── persistence/
    │   ├── mapper/             OrderMapper / OrderItemMapper / OrderStatusLogMapper
    │   └── repository/OrderRepositoryImpl.java   + 仓储实现（CAS 状态更新、状态日志）
    ├── config/                 既有配置类
    └── exception/              基础设施异常转换
```

## 关键业务流程

### 1. 预构建（pre-build，只读、不落库）
`OrderQueryController.preBuild` / `OrderCommandController.preBuild` →
`OrderApplicationService.preBuildOrder` → `OrderPlacementDomainService.preBuild`：

1. 校验门店、顾客是否可用（经 `StoreGateway` / `CustomerGateway`）。
2. 用 `SkuIdParser` 解析 `skuId`，得到商品/选项/客制化项 ID。
3. 经 `ProductGateway` **批量**拉取商品、封面、门店状态、客制化、选项快照。
4. 逐项校验可用性：
   - 商品级剔除（全局下架 / 门店售罄）→ `UnavailableProduct`
   - 选项级剔除（客制化项未激活 / 选项禁用 / **跨绑定异常**）→ `UnavailableCustomization`
5. 有效项计价，封面经 `ProductGateway.findProductCovers` 得到
   `productId → coverId + coverUrl`，封面标识 `coverId` 随明细持久化，封面 URL 仅用于
   展示；产出 `PreBuildResult`（总价、总数量、明细、不可用清单）。

> 设计为**只读、不落库、不占幂等键**：前端可反复调用以刷新购物车。

### 2. 创建订单（create，落库 + 调支付）
`OrderApplicationService.createOrder`：

1. 拦截不支持的支付方式（微信支付暂未实现，抛 `PayErrorCode`）。
2. **幂等**：先查 Redis（`dextea:order:idem:<key>`，TTL 24h）；命中直接返回缓存结果。
3. 复用 `preBuild` 校验数据合法性与计价；门店/顾客不可用直接拒绝；存在不可用项返回 `preBuild` 结果（不创建订单、不占幂等键）。
4. **落库**：`OrderRepository.save`；MySQL 唯一索引对幂等键兜底——若 Redis 校验过期但库已存在，捕获 `DuplicateKeyException` 查回已有订单复用。
5. **支付**：支付宝订单经 `PaymentClientGateway.createPayment` 创建交易并回填 `trade_no`（需顾客已绑定支付宝 `alipayOpenId`，经 `ExternalDataFacade.findCustomer` 读取）。
6. 缓存首次创建结果到 Redis，供后续相同幂等键请求快速返回。

### 3. 订单状态机
`OrderStatusMachine` 用「当前状态 + 事件 → 目标状态」白名单描述合法流转：

- `待支付` --(支付/支付完成/关闭)--> `已支付` / `已结算` / `已关闭`
- `已支付` / `已结算` --(退款)--> `已退款`

不在白名单的组合视为非法流转。状态更新通过 `OrderRepository.updateStatusCas`
以**乐观锁（version + 期望状态）**保证并发安全，并写 `OrderStatusLog` 流水。

## 网关（gateway）清单

| 网关接口（domain.gateway） | 抽象内容 | 实现（infrastructure.gateway.impl） |
|------|----------|----------------------------------|
| `ProductGateway` | 只读商品/客制化/封面快照 | `ProductGatewayImpl` → `CatalogMapper` |
| `CustomizationGateway` | 只读客制化/选项快照 | `CustomizationGatewayImpl` → `CatalogMapper` |
| `StoreGateway` | 只读门店快照 | `StoreGatewayImpl` → `CatalogMapper` |
| `CustomerGateway` | 只读顾客快照 | `CustomerGatewayImpl` → `CatalogMapper` |
| `PaymentClientGateway` | 创建支付交易 | `PaymentClientAdapter` → pay 应用 `PaymentService` |
| `OrderRepository` | 订单持久化（含 CAS） | `OrderRepositoryImpl` + Mapper |
| `OrderIdGeneratorGateway` | 分布式订单号 | `OrderIdGeneratorAdapter` → CosId |
| `OrderLockGateway` | 分布式锁 | `OrderLockAdapter` → Redis |
| （支付结果回写） | 支付成功/关闭同步 | `OrderPaymentSyncAdapter` 实现 pay 域的 `PaymentResultSyncGateway` |

> 设计核心：**order 领域层完全不 import 任何基础设施或第三方 SDK**，所有外部能力
> （DB、Redis、pay、商品/客制化/门店/顾客）都通过 `gateway` 接口抽象。要替换支付渠道、
> ID 生成器、DB 框架或商品数据源，只需换 `infrastructure.gateway.impl` 的实现，不动领域代码。

## 图库隔离（ACL 设计要点）

商品封面涉及 `product_image`（商品↔图库绑定）与 `gallery`（图库地址）的两段 join。
该表结构对领域层是**噪声**：

- 基础设施层 `ProductGatewayImpl` 同时查询 `product_image` 与 `gallery` 两张表，
  交由 `ProductTranslator` 完成 join，产出**清洗后**的 `ProductCover(productId, coverId, coverUrl)`。
- 领域层只消费 `ProductCover`，对 `GalleryPO` / `ProductImagePO` 零感知；
  `coverId`（封面标识）作为不透明持久化字段落库，`coverUrl` 仅在查询/展示时经
  `ProductGateway.findCoverUrls` 还原。

## 依赖方向

```
api → application → domain ← infrastructure
```

领域层位于最内层、最稳定，所有外部依赖都通过 `domain/gateway` 定义接口，由
`infrastructure/gateway/impl` 实现。

## 扩展指引

- 新增订单字段：在 `domain/model/aggregate/Order`（及 `entity/OrderItem`）加属性 →
  同步应用层 `dto/` 与 `OrderAssembler` → `infrastructure/persistence` Mapper 的 SQL。
- 新增状态流转：在 `enums` + `OrderStatusMachine.RULES` 注册，并确认
  `PaymentResultSyncGateway` 适配器映射正确。
- 接入新支付渠道：实现 order 侧 `PaymentClientAdapter` 与 pay 侧 `PaymentGateway`。
- 扩展商品/门店/顾客数据源：在 `infrastructure/gateway/impl/*GatewayImpl` 调整
  `CatalogMapper` 查询与 `ProductTranslator` 等映射，领域层不受影响。
