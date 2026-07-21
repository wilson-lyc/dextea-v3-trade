---
name: 统一OrderService两个接口的响应与构建逻辑
overview: 针对 OrderService 的 preBuildOrder 与 createOrder 两个接口，抽离二者响应 DTO 中重复的 4 个字段到公共基类，并合并 createOrder 中两处重复的响应构建逻辑，增强代码复用、统一对外表现（JSON 结构不变）。
todos:
  - id: create-base-dto
    content: 新增 AbstractOrderResponse 基类并重构两个响应 DTO 继承它
    status: completed
  - id: unify-response-builder
    content: 在 OrderServiceImpl 中统一响应构建 helper 并删除重复 builder 代码
    status: completed
    dependencies:
      - create-base-dto
  - id: compile-verify
    content: 执行 mvn 编译验证序列化与构建无误
    status: completed
    dependencies:
      - unify-response-builder
---

## 用户需求
针对 `OrderService` 中的两个接口（`preBuildOrder` 与 `createOrder`）存在响应结构重叠、构建逻辑重复的问题，进行重构改进，增强代码复用能力，统一对外表现。

## 产品概述
两个下单相关接口共用同一套「预构建」核心逻辑（校验、解析 skuId、查表、分类剔除不可用项、计价），但对外返回的两个响应对象 `PreBuildOrderResponse` 与 `CreateOrderResponse` 重复声明了相同的 4 个字段（`unavailable`、`products`、`totalQuantity`、`totalPrice`），且 `createOrder` 内部将 `PreBuildOrderResponse` 映射为 `CreateOrderResponse` 的构建代码写了两处（不可用分支与正常创建分支），存在重复与不一致风险。

## 核心特性
- 提取两个响应对象的公共字段到统一基类，消除 DTO 字段重复定义。
- 统一 `PreBuildOrderResponse` → `CreateOrderResponse` 的映射构建逻辑，使用单一 helper 覆盖「不可用」与「正常创建」两种场景。
- 保证对外 JSON 响应结构保持扁平、API 契约不变。


## 技术栈
- 语言：Java 17
- 框架：Spring Boot（@Service / @RestController）
- 工具：Lombok（`@Builder` / `@SuperBuilder` / `@Data`）、Jackson（JSON 序列化）、MyBatis
- 构建：Maven（pom.xml / mvnw）

## 实现方案
### 总体策略
采用「继承 + Lombok `@SuperBuilder`」而非组合嵌套的方式统一两个响应对象。新增公共基类 `AbstractOrderResponse` 持有 4 个共享字段；`PreBuildOrderResponse` 与 `CreateOrderResponse` 继承该基类。父、子均使用 `@SuperBuilder`，Jackson 对继承字段的序列化仍为扁平结构（字段出现在同一层级），因此对外 API 契约完全不变。

### 关键技术决策与权衡
1. **为何用继承而非组合嵌套**：组合（在 `CreateOrderResponse` 内嵌一个 `summary` 字段）会改变 JSON 结构（多一层嵌套），破坏现有接口契约；继承 + `@SuperBuilder` 在保持扁平 JSON 的同时消除字段重复，是最小侵入方案。
2. **`@SuperBuilder` 一致性约束**：Lombok 要求父类与子类必须同时使用 `@SuperBuilder`（不能父类 `@Builder`、子类 `@SuperBuilder`），否则编译失败。基类与两个子类统一改为 `@SuperBuilder`。
3. **统一构建 helper**：在 `OrderServiceImpl` 中新增 `toCreateOrderResponse(PreBuildOrderResponse summary, Order order)`，当 `order == null` 时 `id/orderNo/tradeNo` 置空，覆盖「不可用（不落库）」与「正常创建」两种场景，删除原两处重复的 `CreateOrderResponse.builder()` 代码，消除拷贝遗漏风险。

### 性能与可靠性
- 本次重构仅调整 DTO 结构与内部构建方法，不涉及数据库查询、Redis、支付宝调用等热路径，无性能影响。
- 复用现有 `preBuild` 核心逻辑（已共享），不改动其校验/计价/幂等/支付逻辑，控制改动爆炸半径。

## 实现要点
- 基类使用 `@Getter`/`@Setter` + `@SuperBuilder`（或 `@Data` + `@SuperBuilder`），保留 `@Schema` 注解以便 Swagger 文档继承展示。
- `CreateOrderResponse` 保留 `@JsonIgnoreProperties(ignoreUnknown = true)`。
- 仅 `OrderServiceImpl` 内部构造这两个 DTO（build 调用点：`preBuild` 内 1 处 `PreBuildOrderResponse.builder`、`createOrder` 内 2 处 `CreateOrderResponse.builder`），改基类后字段集不变，调用点安全。
- 控制器 `OrderController` 仅消费响应、不参与构造，不受影响。

## 架构设计
本次为局部 DTO 层与 Service 内部构建逻辑的重构，不涉及整体架构调整。改动范围限定在 DTO 包与 `OrderServiceImpl`，保持现有分层（Controller → Service → Mapper）不变。

## 目录结构
```
src/main/java/cn/dextea/trade/
├── dto/
│   ├── AbstractOrderResponse.java     # [NEW] 公共响应基类。持有 unavailable、products、totalQuantity、totalPrice 四个共享字段，使用 @SuperBuilder + @Getter/@Setter + @Schema，供两个响应类继承，统一对外表现。
│   ├── PreBuildOrderResponse.java     # [MODIFY] 继承 AbstractOrderResponse，移除 4 个重复字段，改用 @SuperBuilder，保留原有 @Schema 描述。
│   └── CreateOrderResponse.java       # [MODIFY] 继承 AbstractOrderResponse，仅保留 id、orderNo、tradeNo 三个订单标识字段，移除 4 个重复字段，保留 @JsonIgnoreProperties 与 @SuperBuilder。
└── service/impl/
    └── OrderServiceImpl.java          # [MODIFY] 新增私有 helper toCreateOrderResponse(summary, order) 统一构建 CreateOrderResponse（order 为 null 时标识字段置空）；删除 createOrder 中不可用分支与 toResponse 方法里的两处重复 builder 代码，统一调用该 helper。
```

## 关键代码结构
```java
// 公共基类：统一两个响应对象的共享字段与构建器
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractOrderResponse {
    private CreateOrderUnavailable unavailable;
    private List<CreateOrderProductItem> products;
    private Integer totalQuantity;
    private BigDecimal totalPrice;
}

// 统一构建 helper（位于 OrderServiceImpl）
private CreateOrderResponse toCreateOrderResponse(PreBuildOrderResponse summary, Order order) {
    return CreateOrderResponse.builder()
            .id(order != null ? order.getId() : null)
            .orderNo(order != null ? order.getOrderNo() : null)
            .tradeNo(order != null ? order.getTradeNo() : null)
            .unavailable(summary.getUnavailable())
            .products(summary.getProducts())
            .totalQuantity(summary.getTotalQuantity())
            .totalPrice(summary.getTotalPrice())
            .build();
}
```

