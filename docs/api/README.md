# 接口文档索引

> 接口文档手动维护，一个接口一个 md 文件。新增或修改文档必须遵循 [example.md](./example.md) 的格式规范。

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

`code = 0` 表示成功；非 `0` 见各接口文档的错误码字典。HTTP 状态码由错误码段统一决定：`2xxxxx → 400`；`4xxxxx` 小段与 HTTP 4xx 对齐（`401xx→401`、`404xx→404`、`409xx→409`、`429xx→429`，其余 `→400`）；`3xxxxx` / `5xxxxx → 500`。

### 枚举字典

以下枚举值在多个接口中出现，全站统一：

| 枚举 | 取值 |
| ---- | ---- |
| 用餐方式 diningMethod | `1` 堂食，`2` 外带 |
| 订单来源 source | `0` 线下，`1` 支付宝，`2` 微信，`3` APP |
| 支付方式 paymentMethod | `0` 现金，`1` 支付宝，`2` 微信 |
| 制作状态 makingStatus | `0` 待制作，`1` 制作中，`2` 制作完成，`3` 已取餐，`4` 已取消 |
| 支付状态 paymentStatus | `0` 支付中，`1` 支付超时，`2` 已支付，`3` 退款中，`4` 已退款 |

### 全站错误码总表

### 错误码分段规范

错误码为 5 位数字，按「首位大类 + 第 2-3 位模块码 + 第 4-5 位具体错误」分段：

| 首位 | 大类 | HTTP 状态码 | 说明 |
| ---- | ---- | ----------- | ---- |
| 0 | 成功 | 200 | — |
| 2 | 业务错误 | 400 | 统一业务前缀，第 2-3 位为模块码 |
| 3 | 第三方 / 下游服务错误 | 500 | 第 2-3 位为渠道码 |
| 4 | 客户端错误 | 小段与 HTTP 4xx 对齐 | `400xx` 参数、`401xx` 鉴权、`404xx` 资源不存在、`409xx` 冲突幂等、`429xx` 限流 |
| 5 | 系统错误 | 500 | `500xx` 通用系统、`501xx` 存储类 |

业务错误（2 段）模块码分配，新模块向 `23` 递增分配：

| 模块码 | 模块 | 错误码定义 |
| ------ | ---- | ---------- |
| 21 | 订单 | `OrderErrorCode` |
| 22 | 支付 | `PayErrorCode` |

第三方 / 下游错误（3 段）渠道码分配，新渠道向 `31` 递增分配：

| 渠道码 | 渠道 | 错误码定义 |
| ------ | ---- | ---------- |
| 30 | 通用 | `DownstreamErrorCode` |

各接口文档的错误码字典只列本接口可能出现的码；下表为全站汇总，新增或调整错误码后须同步更新。

| 错误码 | HTTP 状态码 | 说明 | 定义位置 |
| ------ | ----------- | ---- | -------- |
| 50000 | 500 | 系统繁忙，请稍后重试 | CommonErrorCode |
| 50100 | 500 | 数据库未启用 | CommonErrorCode |
| 50101 | 500 | 数据库访问异常（系统繁忙，请稍后重试） | CommonErrorCode |
| 21001 | 400 | 顾客不存在 | OrderErrorCode |
| 21002 | 400 | 门店不存在 | OrderErrorCode |
| 21003 | 400 | 商品不存在 | OrderErrorCode |
| 21006 | 400 | 门店未营业 | OrderErrorCode |
| 21007 | 400 | 顾客不可用 | OrderErrorCode |
| 21008 | 400 | 非法的 SKU | OrderErrorCode |
| 21010 | 400 | 非法的用餐方式 | OrderErrorCode |
| 21011 | 400 | 非法的订单来源 | OrderErrorCode |
| 21012 | 400 | 非法的支付方式 | OrderErrorCode |
| 21013 | 400 | 订单项数量非法 | OrderErrorCode |
| 21014 | 400 | 重复提交，请勿重复创建订单 | OrderErrorCode |
| 21016 | 400 | 订单不存在 | OrderErrorCode |
| 21017 | 400 | 该订单不属于当前顾客 | OrderErrorCode |
| 21019 | 400 | 该订单不属于当前门店 | OrderErrorCode |
| 21027 | 400 | 制作状态必须按 待制作→制作中→制作完成→已取餐 逐级变更 | OrderErrorCode |
| 22001 | 400 | 支付宝创建交易失败 | PayErrorCode |
| 22002 | 400 | 支付宝配置缺失 | PayErrorCode |
| 22004 | 400 | 支付宝交易查询失败 | PayErrorCode |
| 30001 | 500 | 下游服务未配置 | DownstreamErrorCode |
| 30002 | 500 | 下游服务不可用 | DownstreamErrorCode |
| 40001 | 400 | 缺少请求头 | CommonErrorCode |
| 40002 | 400 | 参数缺失 / 参数校验失败 / 请求体格式错误 / 参数类型不正确 | CommonErrorCode |
| 40100 | 401 | 未登录 | CommonErrorCode / 鉴权拦截器 |
| 40101 | 401 | 令牌无效 | AuthErrorCode |
| 40102 | 401 | 令牌已过期 | AuthErrorCode |
| 40103 | 401 | 令牌已被禁用 | AuthErrorCode |
| 42901 | 429 | 请求过于频繁 | CommonErrorCode |
| 40901 | 409 | 重复提交，请勿重复操作 | CommonErrorCode |
