# 订单 ID（订单号）生成逻辑

本文档介绍本系统业务订单号（orderNo）的生成方式、格式与调用链路。订单号是贯穿下单、支付、回调、查询等全链路的业务主键。

---

## 1. 概述

订单号由本系统在**创建订单**时生成，具备全局唯一、趋势递增、可读（带日期前缀）的特点：

- **底层算法**：CosId 的 Snowflake（雪花算法），由 CosId 机器号 + 时间戳 + 序列号组成。
- **格式**：`yyyyMMdd` 日期前缀 + Snowflake 数字串，例如 `202608201234567890123456789`。
- **生成时机**：订单创建（`OrderCreationService.createOrder`）且通过商品可用性校验后，调用 `orderNoGenerator.next()` 生成。

---

## 2. 生成规则

代码位置：`cn.dextea.trade.order.infrastructure.adapter.SnowflakeOrderNoGenerator`（实现领域端口 `OrderNoGenerator`）

```java
private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
private static final String PROVIDER_NAME = "order";

@Override
public String next() {
    IdGenerator idGenerator = idGeneratorProvider.getRequired(PROVIDER_NAME);
    String datePrefix = LocalDate.now().format(DATE_FORMAT);
    return datePrefix + Long.toString(idGenerator.generate());
}
```

| 项 | 说明 |
| --- | --- |
| 日期前缀 | 当前日期 `yyyyMMdd`（如 `20260820`） |
| 主体 | CosId 雪花算法生成的 `long`，经 `Long.toString` 转字符串拼接 |
| Provider 名称 | `order`，对应 `application.yaml` 中 `cosid.snowflake.provider.order` 命名空间配置 |
| 返回值 | 字符串，由日期前缀与雪花 ID 直接拼接 |

---

## 3. CosId / 雪花配置

相关配置位于 `application.yaml`：

```yaml
cosid:
  namespace: ${COSID_NAMESPACE:${spring.application.name}}
  machine:
    enabled: ${COSID_MACHINE_ENABLED:true}
    distributor:
      type: ${COSID_MACHINE_DISTRIBUTOR_TYPE:redis}   # 机器号由 Redis 分配/持久化
  snowflake:
    enabled: ${COSID_SNOWFLAKE_ENABLED:true}
    provider:
      order:
        namespace: ${COSID_SNOWFLAKE_PROVIDER_ORDER_NAMESPACE:${spring.application.name}}
```

要点：

- **机器号分配**：通过 `machine.distributor.type=redis` 在 Redis 中分配并持久化机器号，避免多实例部署时机器号冲突导致 ID 重复。
- **命名空间**：雪花 provider 命名为 `order`，与 `PROVIDER_NAME` 对应。
- **可配置项**：`COSID_NAMESPACE`、`COSID_MACHINE_ENABLED`、`COSID_MACHINE_DISTRIBUTOR_TYPE`、`COSID_SNOWFLAKE_ENABLED`、`COSID_SNOWFLAKE_PROVIDER_ORDER_NAMESPACE`。

---

## 4. 调用链路

订单号在下单主流程中生成：

1. `OrderCreationService.createOrder(...)` 构建订单草稿并校验商品可用性。
2. 若存在不可售商品，终止生成订单号、不创建交易，降级为预构建结果（只返回订单数据，不下单）。
3. 商品均可用时，调用 `order.place(orderNoGenerator.next(), source, paymentMethod, diningMethod, note, idempotencyKey)` 写入订单号及其他下单信息。
4. 之后调用支付网关创建交易单（`paymentPort.createTradeNo`，以 `orderNo` 关联），订单落库，并发送支付超时延迟消息。

> 注意：预构建（`preBuildOrder`）阶段**不会**生成订单号，只有正式 `createOrder` 且通过校验才会生成，保证订单号只分配给真实落库的订单。

---

## 5. 唯一性与幂等

- **全局唯一**：依赖 Snowflake 的机器号 + 时间戳 + 序列号，配合 Redis 机器号分配，多实例下不冲突。
- **业务去重**：下单接口使用幂等键（`idempotencyKey`）做双重校验 + Redis 分布式锁 + MySQL 唯一索引兜底，避免同一请求生成重复订单号。
- **与数据库主键**：订单号（`orderNo`）为业务主键，区别于订单表自增主键 `orderId`；支付回调等外部消息以 `orderNo`（`out_trade_no`）关联订单。

---

## 6. 小结

- 订单号 = `yyyyMMdd` + CosId Snowflake ID，全局唯一且带日期可读性。
- 机器号由 Redis 分配，保证分布式部署安全。
- 仅在正式创建订单（商品均可用）时生成，预构建阶段不分配。

---

← [返回 README](../README.md)
