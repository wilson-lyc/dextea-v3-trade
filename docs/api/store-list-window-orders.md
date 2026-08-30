# 查询门店时间窗口订单

## 接口说明

查询门店最近 `hours` 小时内创建的订单列表，供门店制作屏/收银台展示进行中的订单。列表项为轻量摘要（不含订单项明细），需要明细时调用 [查询门店订单详情](./store-get-order-detail.md)。

## 请求地址

```
GET /api/v1/store/orders/window
```

## 请求头

| 请求头 | 必填 | 说明 |
| ------ | ---- | ---- |
| Authorization | 是 | API 访问令牌，格式 `Bearer {token}` |
| X-Store-Id | 是 | 门店 ID，标识当前门店 |

## 请求参数

### 查询参数

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| hours | integer | 是 | 时间窗口大小（小时），返回最近 hours 小时内的订单，如 `24` |

## 请求示例

```bash
curl "http://localhost:8080/api/v1/store/orders/window?hours=24" \
  -H "Authorization: Bearer {token}" \
  -H "X-Store-Id: 1"
```

## 响应格式

`data` 字段：

| 参数 | 类型 | 说明 |
| ---- | ---- | ---- |
| items | array | 订单列表，元素见下方「items 数组元素」 |
| total | long | 订单总数 |

### items 数组元素

| 参数 | 类型 | 说明 |
| ---- | ---- | ---- |
| orderId | long | 订单 ID |
| orderNo | string | 订单号 |
| pickupCode | string | 取餐码 |
| totalPrice | decimal | 订单总价（元） |
| totalQuantity | integer | 订单商品总数量 |
| diningMethod | integer | 用餐方式：`1` 堂食，`2` 外带 |
| makingStatus | integer | 制作状态：`0` 待制作，`1` 制作中，`2` 制作完成，`3` 已取餐，`4` 已取消 |
| paymentStatus | integer | 支付状态：`0` 支付中，`1` 支付超时，`2` 已支付，`3` 退款中，`4` 已退款 |
| createdAt | string | 下单时间，格式 `yyyy-MM-ddTHH:mm:ss` |

## 响应示例

```json
{
  "code": 0,
  "message": "成功",
  "data": {
    "items": [
      {
        "orderId": 1,
        "orderNo": "DX202604230001",
        "pickupCode": "A12",
        "totalPrice": 36.00,
        "totalQuantity": 2,
        "diningMethod": 2,
        "makingStatus": 1,
        "paymentStatus": 2,
        "createdAt": "2026-04-23T15:15:00"
      }
    ],
    "total": 1
  }
}
```

## 错误码字典

| 错误码 | HTTP 状态码 | 说明 |
| ------ | ----------- | ---- |
| 50000 | 500 | 系统繁忙，请稍后重试 |
| 50101 | 500 | 数据库访问异常（系统繁忙，请稍后重试） |
| 40001 | 400 | 缺少请求头 |
| 40002 | 400 | 参数缺失 / 参数校验失败 / 参数类型不正确 |
| 40100 | 401 | 未登录 |
