# pay 模块（支付域）

`pay` 负责与第三方支付渠道对接：发起支付交易、接收支付回单、并把终态结果
同步回订单。当前实现仅接入**支付宝**（`ALIPAY`），结构上已预留多平台扩展（`PlatformEnum`）。

## 目录结构

```
pay/
├── api/                      接口层（入站适配）
│   ├── dto/                 PaymentCallbackData / PaymentCallbackMessage（回单消息体）
│   └── mq/PaymentCallbackConsumer.java   RocketMQ 推送消费者（回单入口）
├── application/             应用层
│   ├── service/             PaymentService / PaymentCallbackService   应用服务接口
│   │   └── impl/            PaymentServiceImpl（发起支付）/ PaymentCallbackServiceImpl（处理回单）
│   └── command/CreatePaymentCommand.java        创建支付命令对象
├── domain/                  领域层
│   ├── enums/PlatformEnum.java                 支付平台枚举（ALIPAY / WEIXIN…）
│   ├── exception/PayErrorCode.java              支付域错误码
│   ├── model/               PaymentResult（支付结果）
│   │   └── aggregate/Payment.java              支付聚合根
│   ├── gateway/             PaymentGateway（发起交易）/ PaymentResultSyncGateway（结果回写订单）
│   └── service/PaymentDomainService.java        根据终态驱动结果同步
└── infrastructure/          基础设施层
    ├── gateway/impl/AlipayPaymentGatewayImpl.java   支付宝 SDK 实现 PaymentGateway
    └── config/              AlipayPaymentConfig / RocketMqConfig（渠道与 MQ 配置）
```

## 关键流程

### 1. 发起支付（被 order 域调用）
`order` 域经 `PaymentClientGateway` 调用 pay 应用 `PaymentServiceImpl.createPayment`
→ 由 `PaymentGateway`（当前实现 `AlipayPaymentGatewayImpl`）调用 `alipay.trade.create`
创建交易单，返回 `trade_no` 回填订单。

### 2. 接收回单（RocketMQ）
`PaymentCallbackConsumer`（接口层 MQ 消费者）收到消息后：
1. 解析 `PaymentCallbackMessage`（JSON）→ 交给 `PaymentCallbackServiceImpl.handleCallback`。
2. 应用服务将消息转为支付结果，委派 `PaymentDomainService.process(PaymentResult)`。
3. 领域服务按终态分支：
   - **支付成功 / 已结算** → 调 `PaymentResultSyncGateway.syncPaid(...)` 回写订单（含 `tradeNo`、`settled` 标志）。
   - **交易关闭** → 调 `PaymentResultSyncGateway.syncClosed(...)`。
   - **非终态（如 WAIT_BUYER_PAY）** → 仅记录日志，不更新，避免阻塞重试。

> 终态判定与「订单事件映射、幂等」属于**订单域知识**，由 `PaymentResultSyncGateway`
> 的订单侧适配器（`order/infrastructure/gateway/impl/OrderPaymentSyncAdapter`）负责，
> 支付域本身不感知订单状态机细节。这正是网关防腐的体现。

## 网关（gateway）

| 网关 | 方向 | 实现 |
|------|------|------|
| `PaymentGateway` | pay 域 → 支付渠道 | `AlipayPaymentGatewayImpl`（支付宝 SDK） |
| `PaymentResultSyncGateway` | pay 域 → 订单域 | `OrderPaymentSyncAdapter`（在 order 模块） |

## 扩展指引

- **接入新渠道**：在 `PlatformEnum` 加平台 → 实现新的 `PaymentGateway` 实现（如 `WeixinPaymentGatewayImpl`）→ 在 `AlipayPaymentConfig` 同层增加渠道配置。注意 `order` 应用层目前对 `WEIXIN` 做了显式拦截（抛 `PAY_PLATFORM_NOT_SUPPORTED`），需同步放开。
- **调整回单处理**：业务逻辑在 `PaymentDomainService` 与 `PaymentCallbackServiceImpl`，MQ 监听配置在 `infrastructure/config/RocketMqConfig` 与 `api/mq/PaymentCallbackConsumer`。
