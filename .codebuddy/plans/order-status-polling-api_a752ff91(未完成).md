---
name: order-status-polling-api
overview: 在订单查询链路新增一个轻量级「订单交易状态」接口（GET /api/v1/orders/{orderId}/status），供前端轮询交易结果。沿用现有 DDD 读路径（Controller → OrderQueryService → OrderQueryServiceImpl → OrderRepository），不触碰领域服务。返回交易状态、制作状态、支付时间、支付过期时间以及 terminal 终态标记，前端据此判断是否停止轮询。
todos:
  - id: create-status-dtos
    content: 新增 OrderStatusDTO 与 OrderStatusResponse 轻量状态 DTO
    status: pending
  - id: impl-get-status
    content: OrderQueryService 增加 getOrderStatus 并在实现中完成校验、描述与 terminal 计算，补充 assembler 映射
    status: pending
    dependencies:
      - create-status-dtos
  - id: expose-status-endpoint
    content: OrderQueryController 暴露 GET /api/v1/orders/{orderId}/status 轮询接口
    status: pending
    dependencies:
      - impl-get-status
---

## 用户需求
创建一个获取订单状态的接口，用于前端轮询交易结果。

## 产品概述
提供一个轻量级只读接口，仅返回订单当前的交易/制作状态与「是否可停止轮询」的终态标记，避免每次轮询都拉取完整订单详情，降低前端高频轮询的链路开销。查询标识采用 `orderId + customerId`，与现有订单详情接口保持一致，并做越权与存在性校验。

## 核心功能
- 支持按 `orderId + customerId` 查询订单轻量状态。
- 订单不存在或不属于该顾客时返回明确业务错误（复用现有 `OrderErrorCode`）。
- 返回内容包含：订单标识（orderId/orderNo/tradeNo）、交易状态（tradeStatus 及中文描述）、制作状态（makingStatus 及中文描述）、支付过期时间（payExpireAt）、支付完成时间（paidAt）、最后更新时间（updatedAt），以及 `terminal` 终态标记（交易非「待支付」即视为轮询可停止）。


## 技术栈选择
- 后端框架：Spring Boot 3.5.16（Java 21）
- 分层架构：项目既有 DDD 分层（api / application / domain / infrastructure），本次仅改动读路径（api + application）
- 辅助组件：Lombok（@Getter @Builder @NoArgsConstructor @AllArgsConstructor）、springdoc-openapi（@Tag/@Operation）、统一响应 `APIResponse`
- 数据访问：复用 `OrderRepository.findById`，不新增数据库表/字段，无需迁移

## 实现方案
沿既有订单读路径新增一个轻量状态查询方法，完全复用现有模式，不引入新架构：
- 在 `OrderQueryController` 新增 `GET /api/v1/orders/{orderId}/status?customerId=...`，与 `getOrderDetail` 同样的 `@PathVariable` + `@RequestParam @NotNull` 入参与 `APIResponse.success` 返回风格。
- 在 `OrderQueryService` 接口与 `OrderQueryServiceImpl` 中新增 `getOrderStatus(orderId, customerId)`：直接调用 `orderRepository.findById` 取聚合，复用既有「存在性 + 越权」校验（不存在抛 `ORDER_NOT_FOUND`，customerId 不匹配抛 `ORDER_ACCESS_DENIED`），用既有 `safeEnumDesc` 取状态中文描述。
- 终态标记 `terminal` 计算规则：当且仅当 `tradeStatus != TradeStatusEnum.TRADE_WAIT_PAY.getCode()` 时 `terminal = true`（即支付完成/超时/退款中/已退款均为终态，前端据此停止轮询）。该规则集中在应用层，便于后续按业务调整。
- 新增应用层 DTO `OrderStatusDTO` 与接口响应 DTO `OrderStatusResponse`，由 `OrderApiAssembler.toStatus` 完成映射，保持与 `toSummary/toDetail` 一致的装配风格。

### 性能与可靠性
- 该接口为高频读接口，但仅需一次 `findById`（主键查询）与内存内枚举映射，无额外远程调用（区别于 `getOrderDetail` 中的 `externalDataFacade` 封面/门店/规格查询），响应体小、开销低，适合轮询。
- 沿用既有统一异常处理（`GlobalExceptionHandler`）+ 参数校验，保证错误可观测且不泄漏敏感信息。

## 实现要点（防回归）
- 严格保持 Lombok 风格与 `@NotNull` 校验注解，与 `getOrderDetail` 完全一致。
- `terminal` 判定以 `TradeStatusEnum.TRADE_WAIT_PAY.getCode()` 常量比较，避免魔法数字；后续新增终态枚举时只需调整此处。
- 不改动 `OrderRepository` 接口、领域服务、命令链路及基础设施层，确保非破坏性新增。
- Swagger 注解补充 `@Operation(summary = "获取订单交易状态（前端轮询交易结果）")`，保持 API 文档完整。

## 架构设计
复用现有读路径依赖链，无新增组件：
OrderQueryController → OrderQueryService(接口) → OrderQueryServiceImpl → OrderRepository(读聚合) → OrderApiAssembler(应用DTO→响应DTO)

```mermaid
flowchart LR
    A[OrderQueryController<br/>GET /orders/{orderId}/status] --> B[OrderQueryService.getOrderStatus]
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
│   │   └── OrderQueryController.java          # [MODIFY] 新增 GET /{orderId}/status 轮询接口，沿用 @Validated/@NotNull 与 APIResponse.success
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

// 接口响应 DTO（与 OrderStatusResponse 包下其他响应同风格）
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

