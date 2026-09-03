# 接口文档索引

以下为各端接口一览与全站公共约定。每个接口的细节见对应文档。

## 顾客端（Header：`X-Customer-Id`）

| 接口 | 方法 | 路径 | 文档 |
| ---- | ---- | ---- | ---- |
| 订单预构建 | POST | /api/v1/customer/orders/pre-build | [customer-pre-build-order.md](./customer-pre-build-order.md) |
| 创建订单 | POST | /api/v1/customer/orders | [customer-create-order.md](./customer-create-order.md) |
| 查询月订单列表 | GET | /api/v1/customer/orders/monthly | [customer-list-monthly-orders.md](./customer-list-monthly-orders.md) |
| 查询订单详情 | GET | /api/v1/customer/orders/{orderId} | [customer-get-order-detail.md](./customer-get-order-detail.md) |
| 查询订单支付状态 | GET | /api/v1/customer/orders/{orderId}/payment-status | [customer-get-payment-status.md](./customer-get-payment-status.md) |

## 门店端（Header：`X-Store-Id`）

| 接口 | 方法 | 路径 | 文档 |
| ---- | ---- | ---- | ---- |
| 查询门店时间窗口订单 | GET | /api/v1/store/orders/window | [store-list-window-orders.md](./store-list-window-orders.md) |
| 查询门店制作看板 | GET | /api/v1/store/orders/making-board | [store-get-making-board.md](./store-get-making-board.md) |
| 查询门店订单详情 | GET | /api/v1/store/orders/{orderId} | [store-get-order-detail.md](./store-get-order-detail.md) |
| 标记订单制作完成 | POST | /api/v1/store/orders/{orderId}/ready | [store-mark-order-ready.md](./store-mark-order-ready.md) |
| 标记订单已取餐 | POST | /api/v1/store/orders/{orderId}/collect | [store-mark-order-collected.md](./store-mark-order-collected.md) |

## 公共约定

### 鉴权

所有接口需携带 API 访问令牌：`Authorization: Bearer {token}`（令牌由环境变量配置，见 `AuthConfig`）。未携带或校验失败返回 `40100 未登录`（HTTP 401）。

### 身份标识

- 顾客端接口：`X-Customer-Id: {customerId}`，缺失返回 `40001`。
- 门店端接口：`X-Store-Id: {storeId}`，缺失返回 `40001`。

### 统一响应信封

```json
{
  "code": 0,
  "message": "成功",
  "data": { }
}
```

`code = 0` 表示成功；非 `0` 见各接口文档的错误码字典与 [error-codes.md](./error-codes.md)。HTTP 状态码由错误码统一推导：`4` 开头取错误码前 3 位（`40002→400`、`40100→401`、`40400→404`、`40901→409`、`42901→429`）；`2` 开头 → 400；`3` / `5` 开头 → 500。

### 枚举字典

以下枚举值在多个接口中出现，全站统一：

| 枚举 | 取值 |
| ---- | ---- |
| 用餐方式 diningMethod | `1` 堂食，`2` 外带 |
| 订单来源 source | `0` 线下，`1` 支付宝，`2` 微信，`3` APP |
| 支付方式 paymentMethod | `0` 现金，`1` 支付宝，`2` 微信 |
| 制作状态 makingStatus | `0` 待制作，`1` 制作中，`2` 制作完成，`3` 已取餐，`4` 已取消 |
| 支付状态 paymentStatus | `0` 支付中，`1` 支付超时，`2` 已支付，`3` 退款中，`4` 已退款 |

### 错误码

错误码分段规范与全站错误码总表见 [error-codes.md](./error-codes.md)：先分通用错误（按参数、鉴权、资源、幂等、限流、系统、数据库、下游细分），再分业务错误（按订单、支付等模块细分）。各接口文档的错误码字典只列本接口可能出现的码。
