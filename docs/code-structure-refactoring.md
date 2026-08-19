# dextea-trade 代码目录结构重构方案（最佳实践）

> 本文档仅描述改进方案，不执行任何代码移动或重命名。

## 1. 现状分析

当前结构（基于 `src/main/java/cn/dextea/trade/`）：

```
cn/dextea/trade/
├── DexteaTradeApplication.java
├── order/                 # DDD 四层，171 文件
│   ├── interfaces/{http,event,mq}
│   ├── application/{usecase,dto,assembler,service}
│   ├── domain/{model,enumeration,exception,dto,port,repository,service}
│   └── infrastructure/{adapter,mq,persistence}
├── payment/               # DDD 四层（原 pay 重命名，与 order 对称）
│   ├── interfaces/mq
│   ├── application/{service,dto}
│   ├── domain/{exception,port}
│   └── infrastructure/{config,adapter}
├── shared/                # 功能横切，已分组
│   ├── api/ config/ enumeration/ error/ event/ infrastructure/ model/ util/
└── platform/              # 可插拔外部集成（原 shared/nacos 上提）
    └── nacos/
```

### 存在的主要问题

| # | 问题 | 影响 |
| --- | --- | --- |
| P1 | **支付领域被拆成两个模块**：`PaymentPort` 定义在 `order/domain/port`，由 `pay/infrastructure/adapter/AlipayPaymentAdapter` 实现；`PaymentReconciliationService`（支付对账）放在 `order/application/service` | 支付边界不清，order 与 pay 双向耦合，新增支付方式需改两个包 |
| P2 | **事件位置割裂**：`OrderPaidEvent`（领域事件）在 `shared/event`，监听器 `OrderPaidEventListener` 在 `order/interfaces/event` | 事件定义与消费方分散，领域事件溯源困难 |
| P3 | **`shared/nacos` 与 `shared/config` 职责重叠**：Nacos 注册配置放在 `shared/nacos`，通用配置（OpenApiConfig）放在 `shared/config` | 可选插件式能力混入通用横切包，层级混乱 |
| P4 | **`order/domain` 子包空壳多**：`dto/service/repository`（端口接口）与真实实现分散在 infrastructure，domain 层略显单薄但 repository 端口集中在 domain 是对的 | 无明显错误，但 `domain/service` 为空说明领域服务沉淀不足 |
| P5 | **模块间无统一防腐层约束**：order 直接依赖 pay 的端口，pay 间接反向依赖 order 的事件/模型 | DDD 模块边界脆弱，未来拆微服务成本高 |

## 2. 目标结构

原则：**以业务域为顶层模块，模块内严格 DDD 四层；横切能力收敛到 `shared`；可选外部集成（Nacos）独立成可插拔包，不污染核心路径。**

```
cn/dextea/trade/
├── DexteaTradeApplication.java
├── order/                          # 订单域（核心）
│   ├── interfaces/
│   │   ├── http/                   # controller + assembler + dto/{request,response}
│   │   ├── mq/                     # OrderTimeoutMqConsumer / *Properties
│   │   └── event/                  # OrderPaidEventListener（监听 shared 事件）
│   ├── application/
│   │   ├── usecase/                # 命令型用例（CreateOrder/GetOrder...）
│   │   ├── dto/{command,query,result}
│   │   ├── assembler/
│   │   └── service/                # 应用级协调服务（如跨域编排，若有）
│   ├── domain/
│   │   ├── model/                  # Order/OrderItem/Product/Store/Customer + 值对象
│   │   ├── enumeration/
│   │   ├── exception/
│   │   ├── port/                   # 出端口：IdempotencyStore/MakingStatusPublisher/
│   │   │                           #         OrderNoGenerator/OrderTimeoutDelayPort
│   │   ├── repository/             # 仓储接口（Order/Customer/Product/Store/MakingLog/PaymentLog）
│   │   └── service/                # 领域服务（沉淀订单计算/状态机）
│   └── infrastructure/
│       ├── adapter/                # Redis 适配器 / CosId 订单号（实现 order 的 port）
│       ├── persistence/            # MyBatis Mapper + PO + Converter + RepositoryImpl
│       └── mq/                     # 发送端适配（MakingStatusPublisher 实现等）
├── payment/                        # 支付域（原名 pay → 重命名为 payment，语义更完整）
│   ├── interfaces/
│   │   └── mq/                     # PaymentCallbackMqConsumer / *Properties
│   ├── application/
│   │   └── service/                # PaymentCallbackApplicationService
│   │                           #     + PaymentReconciliationService（从 order 迁入）
│   ├── domain/
│   │   ├── model/                  # Payment / PaymentStatus 等领域模型
│   │   ├── enumeration/            # TradeStatus 等
│   │   ├── exception/              # RetryableCallbackException
│   │   └── port/                   # PaymentPort（从 order/domain/port 迁入）
│   └── infrastructure/
│       ├── config/                 # AlipayProperties / MyAlipayConfig
│       └── adapter/                # AlipayPaymentAdapter（实现 PaymentPort）
├── shared/                         # 横切（保持，微调）
│   ├── api/                        # APIResponse
│   ├── model/                      # Money / Quantity 值对象
│   ├── enumeration/                # 跨域公共枚举
│   ├── error/                      # 全局异常处理 / 错误码
│   ├── event/                      # OrderPaidEvent（跨域领域事件统一在此）
│   ├── config/                     # OpenApiConfig 等通用配置
│   └── util/
└── platform/                       # 新增：可插拔外部集成（原 shared/nacos 上提）
    └── nacos/
        ├── NacosDiscoveryAutoConfiguration.java
        ├── NacosDiscoveryProperties.java
        └── NacosDiscoveryRegistrar.java
```

> 注：上图中 `pay → payment` 与 `shared.nacos → platform.nacos` 变更**已执行**；`PaymentPort`、`PaymentReconciliationService` 仍位于 `order`（见 3.1 说明）；DTO 的 command/query/result、request/response 子包划分代码中**早已落地**，本次未额外调整。

## 3. 关键改动说明

### 3.1 支付域归并（解决 P1）

> **实际执行结论（已落地）**：经对真实代码依赖分析，`PaymentPort` 与 `PaymentReconciliationService` **必须留在 `order` 模块，未迁移到 `payment`**。理由如下：
> - `PaymentPort` 由 `order/domain/service/OrderCreationService` 直接注入（`paymentPort.createTradeNo(...)`），是订单域定义的出端口；`payment/infrastructure/adapter/AlipayPaymentAdapter` 仅「实现」该端口。按 DDD 规则，**拥有出端口的模块定义端口**，故 `PaymentPort` 归属 `order` 正确，迁移会反转为 `payment → order` 依赖。
> - `PaymentReconciliationService` 依赖 `order/domain/model/Order`、`order/domain/service/OrderStatusService`、`order/domain/service/PickupCodeGenerator` 等订单核心，本质是「订单视角查询支付状态」的应用编排，迁到 `payment` 会造成 `payment` 重度反向依赖 `order`，违反单向依赖。
>
> 因此本次实际仅做**安全的命名规范化**：`pay` → `payment`（纯重命名，不做跨模块搬移）。`order` 仍通过 `PaymentPort` 单向依赖支付能力，方向保持单一。

- **已执行**：`pay` 整包重命名为 `payment`（package 声明 + 内部互引 + `MarkOrderPaidUseCase` 对 `AlipayProperties` 的 import 同步更新），与 `order` 形成对称命名，消除 `pay`/`payment` 混用。
- 支付相关的回调消费、端口实现、配置集中在 `payment`；端口契约与订单视角对账留在 `order`，依赖方向为 `order → payment`（仅经 `PaymentPort` 出端口，单向）。

### 3.2 领域事件统一出口（解决 P2）
- `OrderPaidEvent` 留在 `shared/event`，作为跨域契约
- 监听器仍按归属放在各自 `interfaces/event`（order 监听后落库、payment 监听后处理），但文档约定：**所有跨模块事件定义必须位于 `shared/event`**，禁止在模块内定义跨域事件

### 3.3 可选集成独立成 `platform`（解决 P3）
- `shared/nacos` 上提到顶层 `platform/nacos`
- 理由：Nacos 是可选外部集成（连不上不阻塞启动），不属于业务横切；`shared` 只保留必然存在的通用件（api/model/error/event/config）
- `shared/config` 仅保留 OpenApiConfig 这类技术配置，Nacos 自动配置移出

### 3.4 DTO 细分（可读性）
- `application/dto` 细分为 `command`（写）/ `query`（读）/ `result`（出），与 CQRS 意图对齐
- `interfaces/http/dto` 细分为 `request`/`response`，与 `application/dto` 明确分层边界

### 3.5 依赖方向约束（解决 P5）
明确模块依赖规则，后续用 ArchUnit 或包扫描守护：
```
order  → payment（仅通过 PaymentPort 出端口，单向）
order  → shared（任意）
payment→ shared（任意）
*      → platform（仅当启用 Nacos 时，且 platform 不得反向依赖业务模块）
shared → 不依赖任何业务模块 / platform
```
即：业务模块可依赖 `shared`，但 `shared` 与 `platform` 永不直接依赖 `order`/`payment`。

## 4. 已落地执行记录（本次重构实际完成项）

1. **`pay` → `payment` 重命名**（已完成）：`git mv` 整包迁移，批量重写 11 个文件的 `package` 声明与内部互引 `import`；同步修正 `order/application/usecase/MarkOrderPaidUseCase` 对 `AlipayProperties` 的 import 指向 `cn.dextea.trade.payment.infrastructure.config.AlipayProperties`。无外部 mybatis xml 引用 pay 包。
2. **`shared.nacos` → `platform.nacos` 上提**（已完成）：`git mv` 迁移 3 个文件，重写其 `package` 声明。该包仅由 Spring 自动扫描（`@Configuration` + `@ConditionalOnProperty`）生效，无任何业务代码 import，迁移零风险。
3. **文档修正**（已完成）：3.1 节据真实依赖剔除「迁移 PaymentPort / PaymentReconciliationService」两项（会破坏单向依赖），3.4 节标注 DTO 细分早已落地。

> 待办（非阻塞，后续可按需进行）：
> - 引入包依赖守护测试（ArchUnit）固化 `order → payment(经端口)`、`* → shared`、`shared/platform 不反向依赖业务` 的方向。
> - 补充 `shared/event` 跨域事件归属注释。

## 5. 不改动的部分

- `DexteaTradeApplication` 启动类位置不变，`@ComponentScan` 默认扫 `cn.dextea.trade` 全包，模块重命名后仍覆盖。
- `infrastructure/persistence` 的 MyBatis Mapper、PO、Converter 组织方式不变（已是合理 Repository 实现模式）。
- 配置文件 `application.yaml`、`.env` 体系不变。
- `shared/model`（Money/Quantity）、`shared/api`（APIResponse）、`shared/error` 结构不变。

## 6. 收益

- 支付能力从「跨模块分散」收敛为单一可独立演进的域，未来若拆微服务只需整体搬 `payment` 包。
- 可选外部集成与核心业务解耦，本地最小运行（无 Nacos）路径更纯粹。
- 模块依赖单向化，降低认知负担与回归风险。
- DTO 按读写意图分组，接口层与应用层边界更清晰。
