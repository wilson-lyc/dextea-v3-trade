---
name: order-status-polling-api
overview: 新增轻量「订单交易状态」接口（GET /api/v1/orders/{orderId}/status）供前端轮询，并将订单查询类接口的顾客 ID 统一改为从 Header `X-Customer-Id` 提取（含已有 getOrdersByCustomer、getOrderDetail 一并改造），命令侧接口不动。沿用 DDD 读路径，新增终态标记 terminal。
todos:
  - id: create-status-dtos
    content: 新增 OrderStatusDTO 与 OrderStatusResponse 轻量状态 DTO
    status: completed
  - id: impl-get-status
    content: OrderQueryService 增加 getOrderStatus 并实现校验/描述/terminal，补充 assembler 映射
    status: completed
    dependencies:
      - create-status-dtos
  - id: controller-header-status
    content: OrderQueryController 将 customerId 改从 X-Customer-Id Header 提取，并新增状态轮询接口
    status: completed
    dependencies:
      - impl-get-status
---

## 用户需求
创建一个获取订单交易状态的轻量只读接口，用于前端轮询交易结果。顾客 ID 统一从上游鉴权层写入的 Header `X-Customer-Id` 提取。

## 产品概述
提供一个高频可读的订单状态接口，仅返回订单当前交易/制作状态与「是否可停止轮询」的终态标记，避免每次轮询都拉取完整订单详情，降低高频轮询的链路开销。顾客 ID 全部从 Header `X-Customer-Id` 提取，与既有查询接口的越权/存在性校验保持一致。

## 核心功能
- 新增 `GET /api/v1/orders/{orderId}/status`：按 `orderId`（路径参数）+ `customerId`（Header `X-Customer-Id`）查询订单轻量状态。
- 已有查询接口 `getOrdersByCustomer`、`getOrderDetail` 的顾客 ID 同样改为从 Header `X-Customer-Id` 提取（命令侧接口本次不动）。
- 订单不存在或不属于该顾客时返回明确业务错误（复用现有 `OrderErrorCode`）。
- 返回内容包含：订单标识（orderId/orderNo/tradeNo）、交易状态（tradeStatus 及中文描述）、制作状态（makingStatus 及中文描述）、支付过期时间（payExpireAt）、支付完成时间（paidAt）、最后更新时间（updatedAt），以及 `terminal` 终态标记（交易非「待支付」即视为轮询可停止）。


## 技术栈选择
- 后端框架：Spring Boot 3.5.16（Java 21）
- 分层架构：项目既有 DDD 分层（api / application / domain / infrastructure），本次仅改动读路径（api + application）
- 辅助组件：Lombok（`@Getter @Builder @NoArgsConstructor @AllArgsConstructor`）、springdoc-openapi（`@Tag/@Operation`）、统一响应 `APIResponse`
- 数据访问：复用 `OrderRepository.findById`，不新增数据库表/字段，无需迁移

## 实现方案
沿既有订单读路径新增轻量状态查询，并将查询/状态接口的顾客 ID 提取方式统一切换为 Header，完全复用现有模式，不引入新架构：

1. **顾客 ID 从 Header 提取**：在 `OrderQueryController` 中，将 `getOrdersByCustomer`、`getOrderDetail` 的 `@RequestParam Long customerId` 改为 `@RequestHeader("X-Customer-Id") @NotNull(message = "customerId 不能为空") Long customerId`。类级已存在 `@Validated`，方法级 `@NotNull` 校验在 Header 缺失时由 `GlobalExceptionHandler` 统一拦截返回，行为与现状一致。Header 名称集中使用字面量 `"X-Customer-Id"`，三处统一，避免拼写漂移。
2. **新增状态查询方法**：在 `OrderQueryService` 接口与 `OrderQueryServiceImpl` 新增 `getOrderStatus(orderId, customerId)`：调用 `orderRepository.findById` 取聚合，复用既有「存在性（抛 `ORDER_NOT_FOUND`）+ 越权（抛 `ORDER_ACCESS_DENIED`）」校验，用既有的私有 `safeEnumDesc` 取状态中文描述。
3. **终态标记规则**：`terminal = !Objects.equals(tradeStatus, TradeStatusEnum.TRADE_WAIT_PAY.getCode())`（支付完成/超时/退款中/已退款均为终态，前端据此停止轮询）。规则集中在应用层，便于后续按业务调整；使用枚举常量比较，避免魔法数字。
4. **新增 DTO 与装配**：新增应用层 `OrderStatusDTO` 与接口响应 `OrderStatusResponse`（同风格字段 + `terminal`），由 `OrderApiAssembler.toStatus` 完成映射，保持与 `toSummary/toDetail` 一致的装配风格。
5. **暴露轮询接口**：`OrderQueryController` 新增 `GET /{orderId}/status`，入参 `@PathVariable Long orderId` + `@RequestHeader("X-Customer-Id") @NotNull Long customerId`，`@Operation(summary = "获取订单交易状态（前端轮询交易结果）")`，返回 `APIResponse.success(OrderApiAssembler.toStatus(orderQueryService.getOrderStatus(orderId, customerId)))`。

### 性能与可靠性
- 该接口为高频读接口，仅需一次 `findById`（主键查询）与内存内枚举映射，无额外远程调用（区别于 `getOrderDetail` 中的 `externalDataFacade` 封面/门店/规格查询），响应体小、开销低，适合轮询。
- 沿用既有统一异常处理 + 参数校验，保证错误可观测且不泄漏敏感信息。

## 实现要点（防回归）
- 严格保持 Lombok 四注解风格与 `@NotNull` 校验，与 `getOrderDetail` 一致。
- Header 名称 `"X-Customer-Id"` 在三处控制器方法中完全一致。
- 不改动 `OrderRepository`、领域服务、命令链路（OrderCommandController / AbstractOrderRequest）及基础设施层，确保非破坏性。
- Swagger 注解补充，保持 API 文档完整。

## 架构设计
复用现有读路径依赖链，无新增组件：
`OrderQueryController`（Header 提 customerId）→ `OrderQueryService`（接口）→ `OrderQueryServiceImpl` → `OrderRepository`（读聚合）→ `OrderApiAssembler`（应用 DTO→响应 DTO）

```mermaid
flowchart LR
    A[OrderQueryController<br/>GET /orders/{orderId}/status<br/>customerId from X-Customer-Id Header] --> B[OrderQueryService.getOrderStatus]
    B --> C[OrderQueryServiceImpl]
    C --> D[OrderRepository.findById]
    C --> E[OrderApiAssembler.toStatus]
    E --> F[OrderStatusResponse]
```

## 目录结构
```
src/main/java/cn/dextea/trade/order/
├── api/
│   ├── controller/
│   │   └── OrderQueryController.java          # [MODIFY] 将 getOrdersByCustomer/getOrderDetail 的 customerId 改为 @RequestHeader("X-Customer-Id")；新增 GET /{orderId}/status 轮询接口
│   ├── dto/response/
│   │   └── OrderStatusResponse.java           # [NEW] 接口响应 DTO（轻量状态 + terminal），Lombok 四注解风格
│   └── assembler/
│       └── OrderApiAssembler.java             # [MODIFY] 新增 toStatus(OrderStatusDTO) → OrderStatusResponse 映射
├── application/
│   ├── dto/
│   │   └── OrderStatusDTO.java                # [NEW] 应用层轻量状态 DTO，承载 orderId/orderNo/tradeNo/状态/时间/terminal
│   └── service/
│       ├── OrderQueryService.java             # [MODIFY] 接口新增 getOrderStatus(Long orderId, Long customerId)
│       └── impl/
│           └── OrderQueryServiceImpl.java     # [MODIFY] 实现 getOrderStatus：校验 + safeEnumDesc + terminal 计算
```

## 关键代码结构
```java
// 应用层 DTO（与 OrderSummaryDTO 同风格）
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderStatusDTO {
    private Long orderId;
    private String orderNo;
    private String tradeNo;
    private Integer tradeStatus;
    private String tradeStatusDesc;
    private Integer makingStatus;
    private String makingStatusDesc;
    private LocalDateTime payExpireAt;
    private LocalDateTime paidAt;
    private LocalDateTime updatedAt;
    private Boolean terminal;
}

// 接口响应 DTO（与 response 包下其他响应同风格）
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderStatusResponse {
    private Long orderId;
    private String orderNo;
    private String tradeNo;
    private Integer tradeStatus;
    private String tradeStatusDesc;
    private Integer makingStatus;
    private String makingStatusDesc;
    private LocalDateTime payExpireAt;
    private LocalDateTime paidAt;
    private LocalDateTime updatedAt;
    private Boolean terminal;
}
```

