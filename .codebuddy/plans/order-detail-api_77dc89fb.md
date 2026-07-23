---
name: order-detail-api
overview: 新增 GET /api/v1/orders/{orderId} 订单详情接口：入参订单主键 id + 顾客ID，校验订单归属后返回「订单基础信息 + 门店信息 + 商品明细」的完整响应结构，用于订单详情页。
todos:
  - id: add-error-codes
    content: 在 OrderErrorCode 新增 ORDER_NOT_FOUND 与 ORDER_ACCESS_DENIED
    status: completed
  - id: add-mapper-methods
    content: 为 OrderMapper 新增 selectById，为 OrderItemMapper 新增 selectFullByOrderId
    status: completed
    dependencies:
      - add-error-codes
  - id: add-response-models
    content: 新建 OrderDetailResponse、StoreInfo、OrderDetailItem 响应模型并加 @Schema
    status: completed
    dependencies:
      - add-error-codes
  - id: impl-service
    content: 实现 OrderService 接口方法与 OrderServiceImpl 校验组装逻辑
    status: completed
    dependencies:
      - add-mapper-methods
      - add-response-models
  - id: add-controller
    content: 在 OrderController 新增 GET /{orderId} 接口并接入校验
    status: completed
    dependencies:
      - impl-service
---

## 用户需求
新增一个订单详情查询接口，用于订单详情页展示。接口入参为订单 ID（数据库主键 `id`，Long 类型）与顾客 ID（Long 类型）。接口必须先校验「订单所属顾客 == 入参顾客 ID」，校验不通过则拒绝访问。响应需设计完整结构，包含订单基本信息、内嵌门店信息、以及商品明细列表。

## 产品概述
为前端订单详情页提供数据支撑的只读查询接口。顾客只能查询自己名下的订单，既保证数据隔离也避免越权访问。

## 核心特性
- 新增 GET 接口按订单主键查询详情，入参同时携带 customerId 用于归属校验。
- 归属校验：订单记录的 customerId 必须与入参 customerId 完全一致，否则返回「订单不属于该顾客」业务异常。
- 订单不存在时返回「订单不存在」业务异常。
- 响应结构包含：订单主信息（订单号、交易号、状态及状态文案、支付方式及文案、用餐方式及文案、金额、数量、备注、各时间字段）、内嵌门店信息（门店名、地址、电话、营业时间）、商品明细列表（商品名、SKU、客制化文本、封面图 URL、单价、数量、小计）。


## 技术栈
- 后端框架：Spring Boot 3.5（Java 21）
- Web/校验：spring-boot-starter-web + spring-boot-starter-validation（@NotNull 校验入参）
- 数据访问：MyBatis + MySQL
- 序列化/文档：Jackson + springdoc-openapi（@Schema 标注响应字段）
- 统一响应：沿用项目 `APIResponse<T>` 与 `BizError` 异常体系

## 实现策略
- **端点设计**：在 `OrderController` 沿用 `@RequestMapping("/api/v1/orders")`，新增 `@GetMapping("/{orderId}")`，使用 `@PathVariable Long orderId` + `@RequestParam @NotNull Long customerId`，保持与现有 `getOrdersByCustomer` 一致的分层模式（Controller → Service 接口 → Service 实现）。
- **归属校验**：查询订单后首先比对 `order.getCustomerId().equals(customerId)`，不一致抛出新增错误码 `ORDER_ACCESS_DENIED`，这是本需求的核心安全点。
- **响应结构**：新增 `OrderDetailResponse`（订单主信息 + `StoreInfo` 门店 + `List<OrderDetailItem>` 明细），所有响应模型沿用 `@Getter @Setter @SuperBuilder @NoArgsConstructor` + `@Schema(description=...)` 约定（参照 `OrderSummary` / `CreateOrderResponse`）。
- **可读文案回填**：利用已有 `OrderStatusEnum.of(code).getDescription()`、`PayMethodEnum.of(code).getDescription()`、`DiningMethodEnum.of(code).getDescription()` 回填 `statusDesc` / `payMethodDesc` / `diningMethodDesc`，前端无需再做枚举映射。
- **封面图解析**：复用现有 `getOrdersByCustomer` 中的思路——按明细 `coverId` 批量查 `gallery` 表并映射为 `coverUrl`，一次性批量查询避免 N+1。
- **性能**：单次订单查询为 O(1) 主表查询 + 1 次门店查询 + 1 次明细批量查询 + 1 次封面批量查询，无 N+1；明细与封面均按 ID 集合去重后批量加载。

## 实现注意事项
- 复用 `OrderMapper`/`OrderItemMapper`/`StoreMapper`/`GalleryMapper` 既有方法；`StoreMapper.selectById` 已在 `getOrdersByCustomer` 中使用，可直接复用。
- 不返回顾客信息（依澄清结果），customerId 仅用于权限校验；但校验前仍需确保 order 非 null，避免空指针。
- 枚举 `of(code)` 在 code 未知时会抛 `IllegalArgumentException`，需用 `of(...)` 前判空或捕获，避免因脏数据导致 500；建议用 `OrderStatusEnum.of(order.getStatus())` 判空后取 description。
- 新增错误码务必接入 `OrderErrorCode`（实现 `BizErrorCode`），由 `GlobalExceptionHandler` 统一转 `APIResponse.error`，保持异常契约一致。
- 仅新增/小改既有 Mapper 方法，不改动下单、预构建等既有逻辑，控制改动半径。

## 架构设计
在既有分层架构上扩展单个查询能力，不涉及架构模式变更：
`OrderController.getOrderDetail` → `OrderService.getOrderDetail` → `OrderServiceImpl`（校验 + 组装）→ `OrderMapper` / `OrderItemMapper` / `StoreMapper` / `GalleryMapper`。
数据流向：入参校验 → 查订单 → 归属校验 → 并行/顺序加载门店与明细 → 批量解析封面 URL → 枚举回填文案 → 组装 `OrderDetailResponse` → `APIResponse.success`。

## 目录结构与文件清单
```
src/main/java/cn/dextea/trade/
├── controller/
│   └── OrderController.java              # [MODIFY] 新增 getOrderDetail(@PathVariable orderId, @RequestParam @NotNull customerId)，返回 APIResponse<OrderDetailResponse>
├── service/
│   └── OrderService.java                 # [MODIFY] 接口新增 getOrderDetail(Long orderId, Long customerId) 方法签名
├── service/impl/
│   └── OrderServiceImpl.java             # [MODIFY] 实现归属校验、门店加载、明细加载+封面解析、枚举文案回填；复用既有 mapper
├── mapper/
│   ├── OrderMapper.java                  # [MODIFY] 新增 selectById(Long id) 按主键查询订单
│   └── OrderItemMapper.java              # [MODIFY] 新增 selectFullByOrderId(Long orderId)，返回完整字段（含 coverId/productName/skuId 等）
├── error/
│   └── OrderErrorCode.java               # [MODIFY] 新增 ORDER_NOT_FOUND(1016,"订单不存在")、ORDER_ACCESS_DENIED(1017,"订单不属于该顾客")
└── model/
    ├── OrderDetailResponse.java          # [NEW] 订单详情响应：主信息 + statusDesc/payMethodDesc/diningMethodDesc + StoreInfo store + List<OrderDetailItem> items
    ├── StoreInfo.java                    # [NEW] 门店信息：id、name、address、phone、businessHours
    └── OrderDetailItem.java              # [NEW] 商品明细：productId、productName、skuId、coverUrl、customizationText、quantity、unitPrice、subtotal
```

## 关键代码结构
```java
// 订单详情响应（沿用 @Getter @Setter @SuperBuilder @NoArgsConstructor + @Schema）
public class OrderDetailResponse {
    private Long id;
    private String orderNo;
    private String tradeNo;
    private Integer status;
    private String statusDesc;          // OrderStatusEnum.getDescription()
    private BigDecimal totalPrice;
    private Integer totalQuantity;
    private Integer payMethod;
    private String payMethodDesc;       // PayMethodEnum.getDescription()
    private Integer diningMethod;
    private String diningMethodDesc;    // DiningMethodEnum.getDescription()
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private LocalDateTime updatedAt;
    private StoreInfo store;
    private List<OrderDetailItem> items;
}

public class StoreInfo {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String businessHours;
}

public class OrderDetailItem {
    private Long productId;
    private String productName;
    private String skuId;
    private String coverUrl;            // 由 coverId 批量关联 gallery 解析
    private String customizationText;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
```

