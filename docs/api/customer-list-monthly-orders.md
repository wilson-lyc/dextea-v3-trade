# 查询月订单列表

## 接口说明

按自然月分页查询当前顾客的历史订单，并返回当月订单总数与总金额。列表项为轻量摘要（不含订单项明细），需要明细时调用 [查询订单详情](./customer-get-order-detail.md)。

## 请求地址

```
GET /api/v1/customer/orders/monthly
```

## 请求头

| 请求头 | 必填 | 说明 |
| ------ | ---- | ---- |
| Authorization | 是 | API 访问令牌，格式 `Bearer {token}` |
| X-Customer-Id | 是 | 顾客 ID，标识当前登录顾客 |

## 请求参数

### 查询参数

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| year | integer | 是 | 年份，如 `2026` |
| month | integer | 是 | 月份，取值 1-12 |

## 请求示例

```bash
curl "http://localhost:8080/api/v1/customer/orders/monthly?year=2026&month=4" \
  -H "Authorization: Bearer {token}" \
  -H "X-Customer-Id: 10086"
```

## 响应格式

`data` 字段：

| 参数 | 类型 | 说明 |
| ---- | ---- | ---- |
| orders | array | 当月订单列表，元素见下方「orders 数组元素」 |
| totalCount | integer | 当月订单总数 |
| totalAmount | decimal | 当月订单总金额（元），保留两位小数 |

### orders 数组元素

| 参数 | 类型 | 说明 |
| ---- | ---- | ---- |
| id | long | 订单 ID |
| storeName | string | 门店名称 |
| createdAt | string | 下单时间，格式 `yyyy-MM-ddTHH:mm:ss` |
| totalPrice | decimal | 订单总价（元） |
| totalQuantity | integer | 订单商品总数量 |
| makingStatus | integer | 制作状态：`0` 待制作，`1` 制作中，`2` 制作完成，`3` 已取餐，`4` 已取消 |
| paymentStatus | integer | 支付状态：`0` 支付中，`1` 支付超时，`2` 已支付，`3` 退款中，`4` 已退款 |
| covers | array | 订单内商品封面图 URL 列表 |

## 响应示例

```json
{
  "code": 0,
  "message": "成功",
  "data": {
    "orders": [
      {
        "id": 1,
        "storeName": "朝阳旗舰店",
        "createdAt": "2026-04-23T15:45:00",
        "totalPrice": 25.00,
        "totalQuantity": 2,
        "makingStatus": 2,
        "paymentStatus": 2,
        "covers": ["https://example.com/a.jpg", "https://example.com/b.jpg"]
      }
    ],
    "totalCount": 12,
    "totalAmount": 320.00
  }
}
```

## 错误码字典

| 错误码 | HTTP 状态码 | 说明 |
| ------ | ----------- | ---- |
| 50000 | 500 | 系统繁忙，请稍后重试 |
| 50101 | 500 | 数据库访问异常（系统繁忙，请稍后重试） |
| 40001 | 400 | 缺少请求头 |
| 40002 | 400 | 参数缺失 / 参数校验失败（如 month 不在 1-12 之间） |
| 40100 | 401 | 未登录 |
