# 订单创建与预构建逻辑

本文档介绍本系统「下单」这一完整业务，它由**预构建（pre-build）**与**正式创建（create）**两个紧密关联的环节组成，二者共用同一套商品校验与算价能力，区别仅在于是否落库、生成订单号与发起支付。文档先给出总体流程，再分别说明两个环节，最后对照小结。

---

## 1. 总体流程

下单对顾客而言是一个整体：先通过预构建确认购物车可售与总价，再正式提交创建订单并发起支付。两条链路在 `OrderCreationService` 汇聚到同一套商品可用性校验与金额计算逻辑。

```text
顾客选品
   │
   ├─ 预构建 POST /api/v1/orders/pre-build
   │     └─ 校验商品可售 + 算价，返回 available/unavailable/totalPrice（不落库、不付款）
   │
   └─ 正式下单 POST /api/v1/orders
         ├─ 幂等校验（三重防护）
         ├─ 复用同一商品校验，若存在不可售 → 降级为预构建结果返回
         └─ 全部可售 → 生成订单号/交易号 → 落库 → 发支付超时延迟消息
```

两个环节共享：

- **入口控制器**：`OrderController`（`create` / `preBuild`）
- **领域服务**：`OrderCreationService`（`createOrder` / `preBuildOrder`）
- **商品校验与算价**：`ProductRepository` 拉取 SKU 校验可售，`orderAmountService` 计算总额/数量
- **结果结构**：均返回「可售项 / 不可售项 / 总数量 / 总金额」

---

## 2. 预构建（pre-build）

代码位置：`cn.dextea.trade.order.interfaces.http.controller.OrderController#preBuild` → `PreBuildOrderUseCase` → `OrderCreationService.preBuildOrder`

- 路径：`POST /api/v1/orders/pre-build`
- 请求头：`X-Customer-Id`（必填）
- 请求体：`PreBuildOrderRequest`（`storeId` + `items` 等，继承自 `AbstractCreateOrderRequest`；**无** `idempotencyKey`）

处理步骤：

1. 组装 `PreBuildOrderCommand`，调用 `orderCreationService.preBuildOrder(customerId, storeId, skuItems)`。
2. 拉取 SKU 并标记每个商品的可售状态，计算总价与总数量。
3. 按 `available` 拆分商品项为 `availableItems` / `unavailableItems`，返回 `PreBuildOrderResult`。

特点（与正式下单的区别）：

| 维度 | 预构建 |
| --- | --- |
| 幂等校验 | 否 |
| 生成订单号 | 否 |
| 创建交易单 | 否 |
| 落库 | 否 |
| 发支付超时消息 | 否 |
| 用途 | 下单前算价、识别不可售商品，供前端提示与决策 |

响应（`PreBuildOrderResult`）：`available` / `unavailable` / `totalQuantity` / `totalPrice`。

---

## 3. 正式创建（create）

代码位置：`OrderController#create` → `CreateOrderUseCase` → `OrderCreationService.createOrder` → `Order.place(...)`

- 路径：`POST /api/v1/orders`
- 请求头：`X-Customer-Id`（必填）
- 请求体：`CreateOrderRequest`

### 3.1 请求参数（CreateOrderRequest）

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| `idempotencyKey` | String | 非空，≤64 | 客户端生成的幂等键 |
| `storeId` | Long | 非空 | 门店 ID（继承） |
| `items` | List | 非空 | 商品项（`skuId` + 数量） |
| `diningMethod` | Integer | 非空 | 用餐方式 |
| `source` | Integer | 非空 | 订单来源 |
| `paymentMethod` | Integer | 非空 | 支付方式 |
| `note` | String | 选填，≤500 | 订单备注 |

### 3.2 幂等控制

采用**三重防护**（代码位于 `CreateOrderUseCase`）：

| 层级 | 实现 | 冲突动作 |
| --- | --- | --- |
| 首次校验 | `idempotencyStore.exists(key)`（Redis） | 抛 `IDEMPOTENCY_KEY_CONFLICT` |
| 二次校验 | 再次 `idempotencyStore.exists(key)` | 抛 `IDEMPOTENCY_KEY_CONFLICT` |
| 落库兜底 | MySQL 唯一索引（捕获 `DuplicateKeyException`，`errorCode==1062`） | 抛 `IDEMPOTENCY_KEY_CONFLICT` |

- 双重 Redis 校验用于快速拦截重复请求，二次校验降低并发穿透概率。
- 即便并发穿透到数据库，唯一索引保证只有一笔写入成功，其余转 `IDEMPOTENCY_KEY_CONFLICT`。
- 仅在订单**成功落库**后，才将幂等键 `record(key, orderNo)` 写入 Redis；写入失败仅告警，依赖 MySQL 唯一索引兜底。

### 3.3 下单主流程

`CreateOrderUseCase.doCreate` 将命令转换为 `SkuItem` 列表后，交由领域服务处理：

1. **商品可用性校验**：复用与预构建同一套校验逻辑；若存在不可售商品，订单降级为预构建结果（结构同 `PreBuildOrderResult`），**不生成订单号、不创建交易、不落库**，直接返回。
2. **生成订单号**：仅当全部商品可售时，调用 `orderNoGenerator.next()`（CosId Snowflake，见 `order-id-generation.md`）生成 `orderNo`。
3. **创建交易单**：调用 `paymentPort.createTradeNo(orderNo, ...)` 生成支付渠道交易号 `tradeNo`，并与 `orderNo` 关联。
4. **构造订单**：`Order.place(orderNo, source, paymentMethod, diningMethod, note, idempotencyKey)` 写入订单号、下单信息。
5. **计算金额/有效期**：`orderAmountService` 计算总额与数量；`paymentTtlMinutes`（配置 `order.payment_ttl`，默认 15 分钟）决定支付过期时间 `paymentExpiredAt`。
6. **落库**：`OrderRepository` 持久化订单与订单项。
7. **支付超时延迟消息**：`orderTimeoutDelayPort.send(orderNo, paymentExpiredAt)` 发送延迟消息，超时未支付则关单。

> 说明：正式下单在商品不可售时**降级**为预构建结果返回，其结构与 `PreBuildOrderResult` 一致，因此前端可复用同一套展示逻辑提示用户移除不可售商品后重试。

---

## 4. 响应结构（OrderCreateResult / PreBuildOrderResult）

两种结果均包含以下订单级字段：

| 字段 | 说明 |
| --- | --- |
| `available` | 可售商品项列表 |
| `unavailable` | 不可售商品项列表（非空即表示部分商品不可下单） |
| `totalQuantity` | 商品总数量 |
| `totalPrice` | 订单总额 |

正式下单额外返回落库相关字段（`OrderCreateResult`）：

| 字段 | 说明 |
| --- | --- |
| `id` | 数据库主键 `orderId`（预构建/降级时为 null） |
| `orderNo` | 订单号（降级时为 null） |
| `tradeNo` | 交易号（降级时为 null） |
| `paymentExpiredAt` | 支付过期时间 |

判断正式下单是否成功：`order.isCreated() == true`（订单号非空）。降级结果因未生成订单号，前端不应视为下单成功。

---

## 5. 两个环节对照

| 维度 | 预构建 `/pre-build` | 正式下单 `/orders` |
| --- | --- | --- |
| 幂等校验 | 否 | 是（三重防护） |
| 生成订单号 | 否 | 是 |
| 创建交易单 | 否 | 是 |
| 落库 | 否 | 是 |
| 发支付超时消息 | 否 | 是 |
| 商品校验/算价 | 共用同一逻辑 | 共用同一逻辑 |
| 用途 | 下单前算价、识别不可售商品 | 真正创建订单并发起支付 |

---

## 6. 小结

- 下单是一个整体：**预构建**负责可售校验与算价（不落库），**正式创建**在其基础上于商品全部可售时生成订单号、交易号、落库并发起支付超时倒计时。
- 二者共用商品校验与金额计算逻辑；正式下单在遇见不可售商品时复用预构建结果结构降级返回。
- 正式下单幂等由「Redis 双校验 + MySQL 唯一索引」三重保证，键在落库后写入。
- 订单号、取餐码生成见 `order-id-generation.md`、`pickup-code-generation.md`；支付回调与关单见 `payment-callback-mq.md` 与相关文档。
