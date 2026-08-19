# 取餐码生成逻辑

本文档介绍本系统取餐码（pickup code）的生成规则、存储方式与调用链路，便于理解门店出餐屏、取餐提醒等下游如何识别与展示取餐码。

---

## 1. 概述

取餐码是顾客在门店取餐时使用的短码，由本系统在**订单支付成功、状态标记为已支付**时生成，并随订单持久化。它具备以下特征：

- **格式**：固定前缀 `8` + 3 位当日序号，例如 `8001`、`8123`。
- **作用域**：按「门店 + 日期」独立计数，不同门店、不同日期的序号互不影响。
- **生成时机**：订单支付成功后，`MarkOrderPaidUseCase` 调用 `PickupCodeGenerator.generate(storeId, date)` 生成并写入订单。

---

## 2. 生成规则

代码位置：`cn.dextea.trade.order.domain.service.PickupCodeGenerator`

```java
private static final String PREFIX = "8";
private static final int MODULO = 1000;

public String generate(Long storeId, LocalDate date) {
    pickupCodeCounterMapper.incrementAndGet(storeId, date);
    Integer dailyCount = pickupCodeCounterMapper.selectDailyCount(storeId, date);
    if (dailyCount == null) {
        dailyCount = 1;
    }
    int suffix = dailyCount % MODULO;
    return PREFIX + String.format("%03d", suffix);
}
```

| 项 | 说明 |
| --- | --- |
| 前缀 | 固定字符串 `"8"` |
| 序号来源 | 数据库表 `pickup_code_counter` 中「门店 + 日期」的当日累计计数 `daily_count` |
| 取模 | `dailyCount % 1000`，即超过 999 后从 `000` 重新循环 |
| 格式化 | `String.format("%03d", suffix)`，保证 3 位、不足补零 |

### 2.1 序号递增与读取

底层使用 `PickupCodeCounterMapper` 两步操作：

1. `incrementAndGet(storeId, date)`：执行 `INSERT ... ON DUPLICATE KEY UPDATE daily_count = daily_count + 1`，原子地将该门店当天的计数 +1（唯一键为 `store_id + date`）。
2. `selectDailyCount(storeId, date)`：读取递增后的 `daily_count` 作为序号来源。

> 说明：两步操作不在同一 SQL 内，依赖数据库唯一键保证计数单调递增；在高并发下多个请求会各自获得不同的 `daily_count`，因此取餐码天然不重复（除非单日同店订单超过 1000 笔后取模回绕）。

### 2.2 调用入口

取餐码在订单支付成功链路中生成：

- `MarkOrderPaidUseCase.execute()` → `pickupCodeGenerator.generate(order.getStoreId(), LocalDate.now())`
- 生成后由 `OrderStatusService.markPaid(order, paidAt, tradeNo, pickupCode)` 写入订单并落库。

---

## 3. 存储与生命周期

| 维度 | 说明 |
| --- | --- |
| 计数表 | `pickup_code_counter`，字段 `store_id`、`date`、`daily_count` |
| 计数重置 | 按自然日（LocalDate）隔离，跨天自动从 1 重新开始（新行 INSERT） |
| 取餐码落点 | 作为订单字段持久化，订单一旦生成即固定，不随计数表变化 |
| 取模回绕 | 单店单日订单超过 1000 笔时序号回绕到 `000`；若同日同店回绕后撞码，以订单号区分业务主体 |

---

## 4. 小结

- 取餐码 = `8` + 3 位「门店+日期」当日序号。
- 序号由 `pickup_code_counter` 表原子自增提供，跨店跨天独立。
- 在订单支付成功、标记已支付时生成并写入订单，与制作状态流转解耦。

---

← [返回 README](../README.md)
