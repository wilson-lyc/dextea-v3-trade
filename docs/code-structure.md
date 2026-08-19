# dextea-trade 项目代码结构

> 本文档描述项目当前代码结构，便于快速理解代码组织方式。

## 1. 总体分层

项目采用 DDD（领域驱动设计）分层，以业务域为顶层模块，每个业务域内部严格划分为四层：

| 层 | 职责 |
| --- | --- |
| `interfaces` | 对外接口层：HTTP 控制器、MQ 消费者、事件监听器，负责协议适配与请求/响应转换 |
| `application` | 应用层：用例（usecase）、应用服务、DTO 与装配器，编排领域对象完成业务用例 |
| `domain` | 领域层：领域模型、领域服务、端口（port）、仓储接口（repository），业务核心 |
| `infrastructure` | 基础设施层：仓储实现、外部适配器（adapter）、持久化（persistence）、MQ 发送端 |

横切能力收敛到 `shared`（通用件）与 `platform`（可插拔外部集成）两个非业务模块。

## 2. 目录总览

```
cn/dextea/trade/
├── DexteaTradeApplication.java          # 启动类（默认扫描 cn.dextea.trade 全包）
├── order/                               # 订单域（核心业务）
├── payment/                             # 支付域
├── shared/                              # 横切通用能力
└── platform/                            # 可插拔外部集成（Nacos）
```

## 3. 订单域 order

DDD 四层结构，约 171 个 Java 文件。

```
order/
├── interfaces/
│   ├── http/                            # HTTP 接口
│   │   ├── controller/                  #   REST 控制器
│   │   ├── assembler/                   #   请求/响应装配
│   │   └── dto/{request,response,shared}#   接口层 DTO（按读写意图分组）
│   ├── event/                           # 领域事件监听器（如 OrderPaidEventListener）
│   └── mq/                              # MQ 消费者（如 OrderTimeoutMqConsumer）
├── application/
│   ├── usecase/                         # 命令型用例（CreateOrder / GetOrder / MarkOrderPaid ...）
│   ├── service/                         # 应用级协调服务（如 PaymentReconciliationService）
│   ├── assembler/                       # 应用层装配器
│   └── dto/{command,result,shared}      # 应用层 DTO（command=写, result=读）
├── domain/
│   ├── model/                           # 聚合与实体：Order / OrderItem / Product / Store / Customer 等
│   ├── service/                         # 领域服务（订单计算、状态机）
│   ├── port/                            # 出端口：IdempotencyStore / MakingStatusPublisher /
│   │                                    #         OrderNoGenerator / OrderTimeoutDelayPort /
│   │                                    #         PaymentPort（支付能力契约，由 payment 实现）
│   ├── repository/                      # 仓储接口：Order / Customer / Product / Store /
│   │                                    #           MakingLog / PaymentLog
│   ├── dto/                             # 领域层 DTO
│   ├── enumeration/                     # 领域枚举
│   └── exception/                       # 领域异常
└── infrastructure/
    ├── adapter/                         # 出端口实现（Redis 适配、CosId 订单号等）
    ├── mq/                              # MQ 发送端适配（实现 domain 的端口）
    └── persistence/                     # 持久化
        ├── mapper/                      #   MyBatis Mapper
        ├── po/                          #   持久化对象
        ├── converter/                   #   PO ↔ 领域模型转换
        └── repository/                  #   RepositoryImpl（实现 domain 的 repository 接口）
```

要点：
- `domain/port` 定义出端口契约，`infrastructure/adapter` 与 `infrastructure/mq` 提供实现，依赖方向为 domain → infrastructure（经接口倒置）。
- `domain/repository` 仅声明接口，`infrastructure/persistence/repository` 落地实现，遵循经典 Repository 模式。
- `PaymentPort` 属于订单域对支付能力的出端口，由 `payment` 模块实现。

## 4. 支付域 payment

DDD 四层结构，约 11 个 Java 文件，与 `order` 对称命名。

```
payment/
├── interfaces/
│   └── mq/                              # PaymentCallbackMqConsumer / PaymentCallbackMqProperties
├── application/
│   ├── service/                         # PaymentCallbackApplicationService（支付回调应用服务）
│   └── dto/                             # PaymentCallbackMessage（回调消息 DTO）
├── domain/
│   ├── port/                            # OrderPaidEventPublisher（订单支付事件发布端口）
│   └── exception/                       # PayErrorCode / RetryableCallbackException
└── infrastructure/
    ├── config/                          # AlipayProperties / MyAlipayConfig（支付宝配置）
    └── adapter/                         # AlipayPaymentAdapter（实现 order 的 PaymentPort）
                                        # SpringOrderPaidEventPublisher（实现 OrderPaidEventPublisher）
```

要点：
- `AlipayPaymentAdapter` 实现 `order` 模块定义的 `PaymentPort`，故依赖方向为 `payment → order`（单向，经端口契约）。
- `OrderPaidEventPublisher` 是支付域定义的出端口，由 `SpringOrderPaidEventPublisher` 实现，供支付回调后向 `shared/event` 发布 `OrderPaidEvent`。

## 5. 横切模块 shared

通用能力，不含业务逻辑，约 8 个子包。

```
shared/
├── api/                                 # APIResponse（统一响应封装）
├── model/                               # 通用值对象：Money / Quantity
├── enumeration/                         # 跨域公共枚举
├── error/                               # 全局异常处理 / 错误码
├── event/                               # 跨域领域事件：OrderPaidEvent（统一事件出口）
├── config/                              # 通用技术配置：OpenApiConfig 等
├── infrastructure/web/                  # Web 相关基础设施
└── util/                                # 工具类
```

约定：所有跨业务模块的领域事件定义统一放在 `shared/event`，业务模块内的监听器引用该事件，但不得在其中定义跨域事件。

## 6. 可插拔集成 platform

```
platform/
└── nacos/                               # Nacos 服务发现集成（可插拔外部依赖）
    ├── NacosDiscoveryAutoConfiguration.java   # 自动配置（@ConditionalOnProperty 控制启用）
    ├── NacosDiscoveryProperties.java          # 配置属性
    └── NacosDiscoveryRegistrar.java           # 注册逻辑
```

要点：
- 与业务无关，靠 Spring 自动扫描与条件注解生效，本地无 Nacos 亦可启动。
- 不属于核心业务路径，故独立于 `shared`，作为可选集成存在。

## 7. 模块依赖方向

```
order   → payment   （仅经 order.domain.port.PaymentPort 出端口，单向）
order   → shared    （任意）
payment → shared    （任意）
payment → order     （仅经 PaymentPort 契约与 OrderPaidEventPublisher，单向）
*       → platform  （仅当启用 Nacos 时；platform 不反向依赖任何业务模块）
shared  → 不依赖 order / payment / platform
platform→ 不依赖 order / payment / shared
```

依赖原则：业务模块之间仅通过对方定义的「出端口」或 `shared` 中的公共契约交互；横切与集成模块永不直接依赖业务模块，保证领域边界清晰、未来可按模块独立拆微服务。
