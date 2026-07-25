# 订单创建业务流程分析报告

## 当前流程概览

```mermaid
flowchart TD
    A[Receive create-order request] --> B{Redis idempotency check}
    B -->|Hit cache| C[Return cached result]
    B -->|Miss| D[PreBuild]
    D --> E[Validate store & customer legality]
    E --> F[Parse skuId → productId / customizationItemId / customizationOptionId]
    F --> G[Batch query products, customization items, customization options]
    G --> H[Query store-level stock status]
    H --> I[Validate item by item: product availability / option availability / cross-binding]
    I --> J{Any unavailable item?}
    J -->|Yes| K[Return unavailable list, do not create order]
    J -->|No| L[Compute total price & quantity]
    L --> M[INSERT order record]
    M -->|DuplicateKeyException| N[Query existing order, reuse]
    M -->|Success| O{Pay method = Alipay?}
    N --> O
    O -->|Yes| P[Call alipay.trade.create]
    P --> Q[Write back trade_no to order]
    Q --> R[Redis cache result]
    O -->|No (WeChat etc.)| R
    R --> S[Return CreateOrderResponse]
```

---

## 发现的问题

### 🔴 严重缺陷：`productIdList` 索引越界

This is the most critical logic bug. In the `preBuild` method:

```217:266:/workspace/src/main/java/cn/dextea/trade/service/impl/OrderServiceImpl.java
    private PreBuildOrderResponse preBuild(CreateOrderRequest request) {
        // ...
        Set<Long> productIds = new LinkedHashSet<>();
        // ...
        for (CreateOrderProductItem item : items) {
            Long productId = SkuIdParser.parseProductId(item.getSkuId());
            // ...
            productIds.add(productId);
            // ...
        }
        // ...
        List<Long> productIdList = new ArrayList<>(productIds);  // ⚠️ Set dedup may be shorter than items

        for (int i = 0; i < items.size(); i++) {
            // ...
            Long productId = productIdList.get(i);  // 🔴 IndexOutOfBoundsException when duplicate products exist!
```

**问题**：`productIds` 是一个会自动去重的 `LinkedHashSet`。当同一商品以不同的定制组合出现时（例如 `1#2_3` 与 `1#4_5`），`productIdList.size()` = 1 而 `items.size()` = 2，导致 `productIdList.get(1)` 越界。

**修复方向**：维护一个与 `items` 一一对应的 `productId` 列表，类似于 `parsedOptionIds` 与 `parsedItemIds`。

---

### 🟡 缺少订单明细行

The `Order` entity only stores summary info:

```106:116:/workspace/src/main/java/cn/dextea/trade/service/impl/OrderServiceImpl.java
        Order order = Order.builder()
                .orderNo(idGenerator.generate())
                .tradeNo(null)
                .idempotencyKey(idempotencyKey)
                .customerId(request.getCustomerId())
                .storeId(request.getStoreId())
                .status(OrderStatus.PENDING.getCode())
                .payMethod(request.getPlatform().getPayMethod().getCode())
                .price(summary.getTotalPrice())
                .quantity(summary.getTotalQuantity())
                .build();
```

没有 `order_items` 表来记录具体的商品、定制选项、单价与数量。这意味着订单创建后无法得知客户实际购买了什么。

---

### 🟡 未扣减库存

订单创建时不会从 `product_store_status` 表扣减库存。这会导致超卖——同一商品可被多个订单同时购买而没有任何库存约束。

---

### 🟡 未校验门店状态

```343:348:/workspace/src/main/java/cn/dextea/trade/service/impl/OrderServiceImpl.java
    private void validateStore(Long storeId) {
        Store store = storeMapper.selectById(storeId);
        if (store == null) {
            throw new BizError(OrderErrorCode.STORE_ID_INVALID, "门店ID错误: " + storeId);
        }
    }
```

`Store` 实体带有 `status` 字段，但从未被检查。已关闭/停业的门店仍能创建订单。

---

### 🟡 `diningMethod` 字段被丢弃

```35:37:/workspace/src/main/java/cn/dextea/trade/dto/CreateOrderRequest.java
    @NotBlank(message = "diningMethod 不能为空")
    @Schema(description = "仅支持 dine_in 和 takeout", example = "dine_in")
    private String diningMethod;
```

该字段通过了 `@NotBlank` 校验，但在整个 `createOrder` 流程中从未被使用或持久化。`Order` 实体也没有对应字段。这意味着在订单层面丢失了“堂食”与“外带”的业务区分。

---

### 🟡 `isOptionUnavailable` 使用了语义错误的枚举

```456:462:/workspace/src/main/java/cn/dextea/trade/service/impl/OrderServiceImpl.java
    private boolean isOptionUnavailable(CustomizationOption option, Integer storeStatus) {
        boolean globalDisabled = option.getStatus() == null
                || option.getStatus() != CustomizationOptionGlobalStatus.ACTIVE.getCode();
        boolean storeDisabled = storeStatus == null
                || storeStatus != CustomizationOptionGlobalStatus.ACTIVE.getCode();
        return globalDisabled || storeDisabled;
    }
```

`storeStatus` 来自 `customization_option_store_status` 表，应当同 `ProductStoreStatusEnum.AVAILABLE`（门店维度）比较，而非 `CustomizationOptionGlobalStatus.ACTIVE`（全局维度）。虽然两者取值恰好都是 1，但语义错误，后续维护中容易引入缺陷。

---

### 🟢 竞态条件风险

在预构建校验与订单插入之间存在一个时间窗口，期间商品可能在另一笔事务中被下架或售罄。目前没有任何锁机制（如悲观锁/乐观锁）来防止这一问题。对于茶饮场景，该风险相对可控。

---

## ✅ 流程中设计良好的部分

1. **完整的幂等设计**：Redis 快速校验 + MySQL 唯一索引兜底双重保障，并合理降级处理 `DuplicateKeyException`。
2. **清晰的 SKU 解析设计**：`SkuIdParser` 工具类封装了 skuId 解析逻辑，并对非法格式统一抛出异常。
3. **对不可用商品的优雅降级**：当存在不可用的商品/选项时，不会创建订单而是返回一个列表，便于客户端修正后重试。
4. **支付宝支付与订单创建解耦**：支付宝 `trade_no` 在订单创建后异步回写，并通过空值校验逻辑保证幂等。

---

## 总结

| 级别 | 问题 |
|------|------|
| 🔴 严重 | 在商品重复的场景下，`productIdList.get(i)` 抛出 `IndexOutOfBoundsException` |
| 🟡 中等 | 缺少订单明细持久化、未扣减库存、未校验门店状态 |
| 🟡 中等 | `diningMethod` 字段被丢弃、`isOptionUnavailable` 使用了错误的枚举 |
| 🟢 轻微 | 预构建与持久化之间的竞态窗口（对茶饮场景可接受） |

**总体评估**：核心幂等架构与 SKU 解析逻辑设计合理，但存在一个严重的功能性缺陷（`productIdList` 索引越界），以及在业务完整性方面的一些缺失（订单明细、库存扣减、门店状态、diningMethod 等）。
