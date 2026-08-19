# 支付回调 MQ 消费说明

本文档面向支付回调 MQ 的消费端读者，介绍本系统如何消费 RocketMQ 中的支付回调消息：从消息拉取、反序列化、业务校验，到最终驱动订单状态流转的完整链路。本文从代码实现层面说明消费逻辑、消息格式、异常处理与重试策略。

---

## 1. 概述

支付回调 MQ 用于接收第三方支付渠道（如支付宝）落地到 RocketMQ 的支付结果回调消息。本系统作为**消费者**订阅该 Topic，并在确认支付成功后驱动订单进入"已支付/制作中"状态。

- **消息中间件**：RocketMQ（使用官方 Java SDK 的 `SimpleConsumer` 模式，主动轮询拉取）
- **Topic（默认）**：`payment_callback`（可通过配置 `PAYMENT_CALLBACK_MQ_TOPIC` 覆盖）
- **消费组（默认）**：`payment_callback`（可通过配置 `PAYMENT_CALLBACK_MQ_CONSUMER_GROUP` 覆盖）
- **Tag（默认）**：`*`（接收全部 Tag，可通过 `PAYMENT_CALLBACK_MQ_TAG` 覆盖）
- **是否启用**：由配置项 `PAYMENT_CALLBACK_MQ_ENABLED` 控制，默认 **关闭**。关闭时不初始化消费者。

> 说明：本系统的 Alipay 网关在收到支付宝异步通知（notify）后，将回调内容投递到该 Topic；本消费链路只负责"消费"这些消息，不直接对接支付宝的 HTTP 通知接口。

---

## 2. 配置项

以下配置位于 `application.yaml` 的 `payment-callback-mq` 节点，均可经环境变量覆盖：

| 配置项 | 环境变量 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `enabled` | `PAYMENT_CALLBACK_MQ_ENABLED` | `false` | 总开关，关闭则跳过消费者初始化 |
| `endpoints` | `PAYMENT_CALLBACK_MQ_ENDPOINTS` | 无 | RocketMQ 接入点 |
| `namespace` | `PAYMENT_CALLBACK_MQ_NAMESPACE` | 空 | 命名空间（可选） |
| `access-key` | `PAYMENT_CALLBACK_MQ_ACCESS_KEY` | 空 | 鉴权 AccessKey |
| `secret-key` | `PAYMENT_CALLBACK_MQ_SECRET_KEY` | 空 | 鉴权 SecretKey |
| `topic` | `PAYMENT_CALLBACK_MQ_TOPIC` | `payment_callback` | 订阅的 Topic |
| `consumer-group` | `PAYMENT_CALLBACK_MQ_CONSUMER_GROUP` | `payment_callback` | 消费组 |
| `tag` | `PAYMENT_CALLBACK_MQ_TAG` | `*` | 订阅 Tag 过滤表达式 |

---

## 3. 消费者初始化与拉取模型

代码位置：`cn.dextea.trade.payment.interfaces.mq.PaymentCallbackMqConsumer`

- 由 `@Configuration` + `@PostConstruct` 在 Spring 容器启动后调用 `start()` 初始化。
- 若 `enabled=false`，仅打印日志并跳过初始化，不创建任何 RocketMQ 连接。
- 使用 `SimpleConsumer`（而非 PushConsumer），由独立单线程 `consumeExecutor` 在 `consumeLoop()` 中**轮询拉取**：
  - 单次最多拉取 `MAX_RECEIVE_NUM = 16` 条；
  - 拉取等待超时 `RECEIVE_TIMEOUT = 20s`；
  - 拉取或处理异常时，打印日志并 `sleep(1s)` 后继续，不会退出循环。
- 应用关闭（`@PreDestroy` 的 `stop()`）时设置 `running=false`，关闭线程池与 `consumer`，保证优雅停机。

---

## 4. 消息处理流程

单条消息经由 `processMessage → handleMessage → application service` 三层处理：

### 4.1 反序列化（handleMessage）

1. 从 `MessageView` 取出消息体 `ByteBuffer`，转为字节数组。
2. 用 `ObjectMapper` 反序列化为 `PaymentCallbackMessage`（`application.dto.PaymentCallbackMessage`）。
   - 反序列化失败会包装为内部 `NonRetryableException`（见第 6 节），直接确认，避免毒消息死循环。
3. 记录日志：messageId、topic、platform、data。
4. 调用 `PaymentCallbackApplicationService.handle(callbackMessage)`。

### 4.2 业务校验（PaymentCallbackApplicationService.handle）

依次执行：

| 步骤 | 校验逻辑 | 失败处理 |
| --- | --- | --- |
| 1 | `data` 非空 | 抛 `BizError(PAY_CALLBACK_MESSAGE_INVALID)` → 不可重试 |
| 2 | 存在 `out_trade_no`（订单号）与 `trade_no`（渠道交易号） | 同上，不可重试 |
| 3 | `trade_status == "TRADE_SUCCESS"` | 非成功状态直接 `return` 忽略（确认消息） |
| 4 | `total_amount` 可解析为 `BigDecimal` | 解析失败抛 `RetryableCallbackException` → 可重试 |
| 5 | 解析成功 | 发布 `OrderPaidEvent` 事件 |

校验通过后，构造并发布领域事件：

```java
orderPaidEventPublisher.publish(new OrderPaidEvent(
        orderNo, tradeNo, message.platform(), amount));
```

### 4.3 事件驱动订单状态流转

`OrderPaidEventPublisher` 的 Spring 实现（`SpringOrderPaidEventPublisher`）通过 Spring `ApplicationEventPublisher` 发布事件；订单模块中的 `OrderPaidEventListener`（`@EventListener`）监听该事件并调用 `MarkOrderPaidUseCase.execute()`：

1. 按 `orderNo` 查询订单摘要；订单不存在则抛 `RetryableOrderException`（等待重投）。
2. `verifyAmount`：比对回调金额与订单金额（支持 `alipay.force-amount` 强制金额），不一致抛 `BizError(ORDER_PAID_AMOUNT_MISMATCH)`，幂等且不可重试。
3. 生成取餐码，调用 `OrderStatusService.markPaid(...)` 在事务内将订单标记为已支付，并进入制作中状态。

> 至此，一笔支付成功回调完成对订单状态的驱动；后续订单进入制作中后会触发制单 MQ（见 `OrderMakingMQ.md`）。

---

## 5. 消息体格式（PaymentCallbackMessage）

反序列化目标结构（`application.dto.PaymentCallbackMessage`）：

| 字段 | 类型 | 含义 |
| --- | --- | --- |
| `id` | String | 消息 ID |
| `platform` | String | 支付平台标识（如 `alipay`） |
| `trace_id` | String | 链路追踪 ID |
| `timestamp` | Long | 时间戳 |
| `raw_body` | String | 原始回调报文 |
| `headers` | Map<String,String> | 头部信息 |
| `data` | Map<String,String> | 业务数据（核心字段见下表） |

`data` 中关心的关键字段（以支付宝为例）：

| 字段 | 含义 |
| --- | --- |
| `out_trade_no` | 本系统订单号 |
| `trade_no` | 支付渠道交易号 |
| `trade_status` | 交易状态，`TRADE_SUCCESS` 表示支付成功 |
| `total_amount` | 支付金额（字符串，转 `BigDecimal`） |

---

## 6. 异常处理与重试策略

`processMessage` 对 `handleMessage` 抛出的异常分类处理，核心原则是：**可恢复的异常不确认消息，交由 RocketMQ 重投；不可恢复的异常直接确认，避免死循环**。

| 异常类型 | 示例 | 处理动作 |
| --- | --- | --- |
| `NonRetryableException` / `BizError` | 反序列化失败、缺订单号、`trade_status` 非法、金额不一致 | 直接 `ack` 确认，不重投 |
| `RetryableException` | `RetryableCallbackException`（金额解析失败） | **不确认**，等待 RocketMQ 重投 |
| 其他 `Exception` | 未预期异常 | 本地计数 `retryCounter`；累计 `< MAX_RETRY_TIMES(5)` 次不确认重投；达到 5 次后 `ack`，视为转死信 |

- 本消费者未使用 RocketMQ 的 Tag/Keys 级去重，重试次数由本地 `ConcurrentHashMap` 的 `retryCounter` 维护；应用重启后计数清零，依赖下游（订单模块）的金额/状态幂等做最终兜底。
- 消费成功的消息调用 `consumer.ack(message)` 确认并清理计数；`ack` 本身失败仅告警，不阻断流程。

---

## 7. 幂等与一致性说明

- **非成功状态幂等**：`trade_status != TRADE_SUCCESS` 直接忽略确认，天然幂等。
- **重复投递**：RocketMQ 至少一次（at-least-once）语义 + 本地重试计数有限，极端情况下可能出现重复消费。最终一致性由订单侧 `MarkOrderPaidUseCase` 的金额比对与状态机（已支付订单重复标记）保证。
- **顺序性**：无顺序保证，单条消息独立处理，业务正确性不依赖消息顺序。
- **事务边界**：支付回调消费与订单状态变更分属不同模块，通过 Spring 应用事件桥接；订单状态变更在 `@Transactional` 事务内完成，确保"标记已支付"的原子性。

---

## 8. 消费链路小结

1. RocketMQ 投递支付回调消息到 `payment_callback`。
2. `PaymentCallbackMqConsumer` 轮询拉取并反序列化为 `PaymentCallbackMessage`。
3. `PaymentCallbackApplicationService` 校验订单号、交易状态、金额是否成功且合法。
4. 校验通过后发布 `OrderPaidEvent` 领域事件。
5. 订单模块 `OrderPaidEventListener` 监听事件，调用 `MarkOrderPaidUseCase` 在事务内校验金额并标记订单已支付、生成取餐码、进入制作中。
6. 处理成功则 `ack`；可重试异常不确认等待重投，不可重试异常直接确认。

---

← [返回 README](../README.md)
