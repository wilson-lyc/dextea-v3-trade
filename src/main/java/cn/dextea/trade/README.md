# dextea-trade 代码结构与设计说明

本目录是「德贤茶庄」线上点餐系统的**交易端后台服务**源码（`cn.dextea.trade`）。
根类 `DexteaTradeApplication` 为 Spring Boot 启动入口。

## 整体架构：DDD + 六边形（Ports & Adapters）

代码按**领域（domain）**划分为多个相互独立、互不垂直依赖的模块，每个模块内部
再按 **四层** 组织。最值得关注的设计是 **网关（gateway）防腐**：一个领域只依赖
自己定义的接口，不依赖其它领域的实现细节，从而实现低耦合。

```
cn.dextea.trade
├── common/   跨模块通用：统一响应、枚举工具、业务异常、全局异常处理、OpenAPI 配置
├── health/   服务健康自检（无业务逻辑的独立小模块）
├── order/    订单域（核心域，已吸收商品/客制化/门店/顾客支撑数据）
└── pay/      支付域（支付支线）
```

> `catalog`（商品目录支撑域）已重构吸收进 `order` 内：商品/客制化/门店/顾客数据
> 不再作为独立域对外暴露，而是以**只读快照值对象**形式由 `order.domain` 经 `gateway`
> 接口消费，其数据库结构隔离在 `order.infrastructure.gateway`（ACL 防腐层）。

每个业务模块（order / pay）统一的四层划分（order 域示意）：

| 层 | 目录 | 职责 | 依赖方向 |
|----|------|------|----------|
| 接口层 | `api/` | 对外暴露：Controller（命令/查询拆分）、request/response DTO、Assembler 装配 | → application / domain |
| 应用层 | `application/` | 用例编排：应用服务接口与实现、Command 命令对象、应用 DTO、ExternalDataFacade 门面 | → domain |
| 领域层 | `domain/` | 业务内核：model（aggregate/entity/valueobject）、service、gateway（网关接口）、repository、enums、exception、util | 自包含，**不依赖任何具体实现** |
| 基础设施层 | `infrastructure/` | 技术落地：gateway.impl（实现 gateway，含 ACL）、persistence（Mapper/Repository）、config、exception | → domain（实现其 gateway） |

> 依赖方向：**api → application → domain ← infrastructure**。
> 领域层位于最内层、最稳定，所有外部依赖（DB、Redis、第三方支付、其它域）都通过
> `domain/gateway` 定义的接口抽象出去，由 `infrastructure/gateway/impl` 在编译期之外注入。

## 模块间协作关系

```
            api(Controller)                        pay: api(MQ)
                   │                                    │
   order ──────────┼── application ── domain ◀── gateway ──► infrastructure/gateway/impl
                   │                                   │
        ┌──────────┴───────────┐          ┌────────────┴────────────┐
        │ 经 ProductGateway /   │          │ ExternalDataFacade 聚合 │
        │ StoreGateway /        │          │ 四网关只读快照（ACL）   │
        │ CustomerGateway 防腐  │          └─────────────────────────┘
        │ 访问商品/门店/顾客数据 │
        │                       │
   pay ── application ── domain ◀── PaymentResultSyncPort ──► order 域 OrderPaymentSyncAdapter（写回订单状态）
        ▲ 经 PaymentClientGateway ◀── order 调起支付
```

关键防腐点（均在 `order/domain/gateway`）：

- `ProductGateway` / `CustomizationGateway` / `StoreGateway` / `CustomerGateway`：订单域经此
  **只读**消费商品、客制化、门店、顾客数据，底层表结构与图库两段 join 封装在
  `order/infrastructure/gateway`（PO + Translator + `CatalogMapper`），避免订单域污染持久化细节。
- `PaymentClientGateway`：订单域创建支付时调用 pay 应用服务，跨域解耦。
- `OrderRepository` / `OrderIdGeneratorGateway` / `OrderLockGateway`：订单自身的持久化、分布式 ID、分布式锁，由基础设施层适配 MySQL / CosId / Redis。

## 阅读建议

1. 想先理解业务主链路：从 `order/api/controller/OrderCommandController`、`OrderQueryController` 入手，沿调用链到 `order/application` → `order/domain/service`。
2. 想理解领域建模：重点读 `order/domain/model`（聚合根/实体/值对象）与 `order/domain/service/OrderStatusMachine`（状态机）。
3. 想理解模块解耦方式：重点读 `order/domain/gateway` 与各 `order/infrastructure/gateway/impl`。
4. 各子模块另有独立的 `README.md`，详见 `order/`、`pay/`、`common/`、`health/`。
