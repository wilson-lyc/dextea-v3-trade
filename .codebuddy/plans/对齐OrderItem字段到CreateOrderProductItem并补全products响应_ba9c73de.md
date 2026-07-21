---
name: 对齐OrderItem字段到CreateOrderProductItem并补全products响应
overview: 把 CreateOrderProductItem 对齐 OrderItem 的业务字段（productId、productName、coverId、unitPrice、subtotal），保持 skuId/quantity 请求必填校验、新增字段不加校验，做成请求响应两用 DTO；在预构建校验阶段顺手构造有效商品列表，并让预构建与创建订单两个接口都在响应中返回剔除不可用项后的 products 列表。
todos:
  - id: extend-product-item-dto
    content: 扩展 CreateOrderProductItem，新增业务级字段并加 BigDecimal 导入
    status: completed
  - id: add-products-to-responses
    content: 为 PreBuildOrderResponse 与 CreateOrderResponse 新增 products 字段
    status: completed
    dependencies:
      - extend-product-item-dto
  - id: build-available-products
    content: 在 preBuild 有效分支构造并收集 availableProducts
    status: completed
    dependencies:
      - extend-product-item-dto
  - id: wire-products-to-create
    content: createOrder 提前返回与 toResponse 透传 products
    status: completed
    dependencies:
      - add-products-to-responses
      - build-available-products
---

## 用户需求
对照 `OrderItem` 实体，扩展 `CreateOrderProductItem` DTO，使其对齐业务级字段，并作为请求/响应两用 DTO；同时在预构建与创建订单接口的响应中新增 `products` 字段，返回剔除不可用内容后的有效商品列表。

## 产品概述
将 `CreateOrderProductItem` 改造为请求响应两用结构：请求侧保留 `skuId`、`quantity` 的非空/范围校验；响应侧新增 `productId`、`productName`、`coverId`、`unitPrice`、`subtotal` 字段且不做校验。预构建与创建订单接口在原有校验/计价阶段顺手构建有效商品明细，免去后续重复查表，并在响应中一并返回。

## 核心特性
- `CreateOrderProductItem` 新增业务级字段（`productId`、`productName`、`coverId`、`unitPrice`、`subtotal`），请求参数仅 `skuId`、`quantity` 校验，其余字段响应侧不校验，业务逐步补全。
- 预构建接口（`/pre-build`）响应新增 `products` 字段，返回剔除不可用项后的有效商品列表（含已补全的单价、小计等）。
- 创建订单接口（`/api/v1/orders`）响应新增 `products` 字段，复用预构建阶段已构建的有效商品明细。
- 校验阶段（预构建核心逻辑）直接计算并收集有效商品项，避免创建订单时二次查表。


## 技术栈
- 语言/框架：Java 17 + Spring Boot（现有项目）
- 注解与工具：Lombok（`@Data/@Builder/@NoArgsConstructor/@AllArgsConstructor`）、Jakarta Validation（`@NotBlank/@NotNull/@Min`）、Swagger（`@Schema`）
- 金额类型：`java.math.BigDecimal`（与 `OrderItem` 一致）

## 实现方案
### 总体策略
复用现有 `preBuild` 校验/计价逻辑，在“有效商品”分支中直接构造 `CreateOrderProductItem`（填充 `productId`、`productName`、`unitPrice`、`subtotal`，`coverId` 暂留 `null` 待后续补全），收集为 `availableProducts` 列表并随 `PreBuildOrderResponse` 返回；`createOrder` 复用同一 `summary`，在 `toResponse` 及不可用提前返回分支中透传 `products`。

### 关键技术决策
1. **两用 DTO 校验分离**：仅在 `skuId`、`quantity` 上保留校验注解；新增字段不加任何校验注解，依靠 Lombok 生成 getter/setter 在响应序列化时填充，符合“响应参数不做校验、业务慢慢补全”的要求。
2. **字段范围（已确认）**：仅对齐业务级字段 `productId`、`productName`、`coverId`、`unitPrice`、`subtotal`；不引入 `id`、`orderId`、`createdAt`、`updatedAt` 等落库管理字段，避免响应暴露内部持久化信息且预构建阶段无法获得。
3. **`coverId` 暂留空**：当前 `Product` 实体无 `coverId` 字段，无数据源，按“慢慢补全”保持 `null`，不强行造数据。
4. **复用而非重复查表**：`createOrder` 已调用 `preBuild` 并持有 `summary`，直接透传 `summary.getProducts()`，无需在落库阶段再查商品表，达成用户“顺手创建 orderitem、免去重复查表”的目标。

### 性能与可靠性
- `preBuild` 已批量查询商品/客制化（`loadProducts` 等），新增的明细构造为 O(有效商品数) 的纯内存操作，无额外 DB 访问，无性能回归。
- `availableProducts` 仅在有效分支追加，天然排除不可用项，与 `unavailable` 清单互斥，数据一致。
- 金额计算沿用现有 `unitPrice.multiply(BigDecimal.valueOf(quantity))` 并 `setScale(2, HALF_UP)`，与 `totalPrice` 口径一致，避免精度漂移。

## 实现注意事项
- 新增 `import java.math.BigDecimal;` 到 `CreateOrderProductItem`，否则编译失败。
- `PreBuildOrderResponse` 与 `CreateOrderResponse` 的 `products` 字段类型统一为 `List<CreateOrderProductItem>`，并加 `@Schema` 描述。
- `createOrder` 中不可用提前返回（L95-103）与 `toResponse`（L153-162）两处均需透传 `products`，保证两种返回路径都带商品明细。
- 保持 `@JsonIgnoreProperties(ignoreUnknown = true)`（CreateOrderResponse 已有）以兼容历史缓存反序列化。

## 架构设计
本变更为局部增强，不引入新架构模式，完全复用现有分层（Controller → Service → DTO/Entity）。数据流：
`CreateOrderRequest.products` → `preBuild` 校验/计价并构建 `availableProducts` → `PreBuildOrderResponse.products` / `CreateOrderResponse.products`。

## 目录结构
```
src/main/java/cn/dextea/trade/
├── dto/
│   ├── CreateOrderProductItem.java   # [MODIFY] 新增 productId、productName、coverId、unitPrice、subtotal 字段（无校验注解），保留 skuId/quantity 校验；新增 BigDecimal 导入。
│   ├── PreBuildOrderResponse.java    # [MODIFY] 新增 List<CreateOrderProductItem> products 字段及 @Schema 描述，builder 中补充 products。
│   └── CreateOrderResponse.java      # [MODIFY] 新增 List<CreateOrderProductItem> products 字段及 @Schema 描述，builder 中补充 products。
└── service/impl/
    └── OrderServiceImpl.java         # [MODIFY] preBuild 中新增 availableProducts 收集（有效分支构造 CreateOrderProductItem 并填充 productId/productName/unitPrice/subtotal，coverId 留 null），返回至 PreBuildOrderResponse；createOrder 的提前返回与 toResponse 透传 products。
```

## 关键代码结构
```java
// CreateOrderProductItem 新增字段（无校验注解，仅响应填充）
private Long productId;
private String productName;
private Long coverId;          // 暂留 null，后续补全
private BigDecimal unitPrice;
private BigDecimal subtotal;
```

