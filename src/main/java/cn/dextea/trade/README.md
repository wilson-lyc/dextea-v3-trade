# dextea-trade 代码结构与设计说明

本目录是「德贤茶庄」线上点餐系统的**交易端后台服务**源码（`cn.dextea.trade`）。
根类 `DexteaTradeApplication` 为 Spring Boot 启动入口。

## 整体架构：DDD + 六边形（Ports & Adapters）

代码按**领域（domain）**划分为多个相互独立、互不垂直依赖的模块，每个模块内部
再按 **四层** 组织。最值得关注的设计是 **端口（port）防腐**：一个领域只依赖
自己定义的接口，不依赖其它领域的实现细节，从而实现低耦合。

```
cn.dextea.trade
├── common/   跨模块通用：统一响应、枚举工具、业务异常、全局异常处理、OpenAPI 配置
├── health/   服务健康自检（无业务逻辑的独立小模块）
├── catalog/  商品目录域（支撑域 / 只读参考数据）
├── order/    订单域（核心域）
└── pay/      支付域（支付支线）
```

每个业务模块（catalog / order / pay）统一的四层划分：

| 层 | 目录 | 职责 | 依赖方向 |
|----|------|------|----------|
| 接口层 | `interfaces/` | 对外暴露：Controller、DTO、MQ 消费、Assembler 装配 | → application / domain |
| 应用层 | `application/` | 用例编排：应用服务接口与实现、Command 命令对象 | → domain |
| 领域层 | `domain/` | 业务内核：model、service、port（端口）、enums、exception、util | 自包含，**不依赖任何具体实现** |
| 基础设施层 | `infrastructure/` | 技术落地：adapter（实现 port）、persistence（Mapper/Repository）、config | → domain（实现其 port） |

> 依赖方向：**interfaces → application → domain ← infrastructure**。
> 领域层位于最内层、最稳定，所有外部依赖（DB、Redis、第三方支付、其它域）都通过
> `domain/port` 定义的接口抽象出去，由 `infrastructure/adapter` 在编译期之外注入。

## 模块间协作关系

```
            interfaces(Controller/MQ)                 interfaces(MQ)
                   │                                         │
   order ─────────┼── application ── domain ◀── port ──► infrastructure/adapter
                   │                              │
        ┌──────────┴───────────┐                  └──► catalog 域（只读快照，经 *Port 防腐）
        │   经 ProductCatalogPort / StorePort /   └──► pay 应用（经 PaymentClientPort）
        │   CustomerPort 防腐访问 catalog 数据
        │
   pay ── application ── domain ◀── PaymentResultSyncPort ──► order 域适配器（写回订单状态）
```

关键防腐点（均在 `order/domain/port`）：

- `ProductCatalogPort` / `StorePort` / `CustomerPort`：订单域经此**只读**消费 catalog 域的商品、门店、顾客数据，避免订单域污染 catalog 持久化细节（实现见 `order/infrastructure/adapter/*Adapter` 委托 `catalog.domain.service.CatalogQueryService`）。
- `PaymentClientPort`：订单域创建支付时调用 pay 应用服务，跨域解耦。
- `OrderRepository` / `OrderIdGeneratorPort` / `OrderLockPort`：订单自身的持久化、分布式 ID、分布式锁，由基础设施层适配 MySQL / CosId / Redis。

## 阅读建议

1. 想先理解业务主链路：从 `order/interfaces/controller/OrderController` 入手，沿调用链到 `order/application` → `order/domain/service`。
2. 想理解领域建模：重点读 `order/domain/model`（值对象/实体）与 `order/domain/service/OrderStatusMachine`（状态机）。
3. 想理解模块解耦方式：重点读 `order/domain/port` 与各 `infrastructure/adapter`。
4. 各子模块另有独立的 `README.md`，详见 `catalog/`、`order/`、`pay/`、`common/`、`health/`。
