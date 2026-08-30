# 查询门店订单详情

## 接口说明

门店查询单个订单的完整信息，用于制作与出餐核对。仅能查询归属本门店（`X-Store-Id`）的订单，否则返回 `21019`。注意：`items[].customization` 返回商品的**原始客制化值**，不做格式转换，门店端需按自己的规则渲染。

## 请求地址

```
GET /api/v1/store/orders/{orderId}
```

路径参数：

| 参数 | 类型 | 说明 |
| ---- | ---- | ---- |
| orderId | long | 订单 ID |

## 请求头

| 请求头 | 必填 | 说明 |
| ------ | ---- | ---- |
| Authorization | 是 | API 访问令牌，格式 `Bearer {token}` |
| X-Store-Id | 是 | 门店 ID，标识当前门店 |

## 请求示例

```bash
curl http://localhost:8080/api/v1/store/orders/1 \
  -H "Authorization: Bearer {token}" \
  -H "X-Store-Id: 1"
```

## 响应格式

`data` 字段：

| 参数 | 类型 | 说明 |
| ---- | ---- | ---- |
| id | long | 订单 ID |
| orderNo | string | 订单号 |
| tradeNo | string | 支付渠道交易号 |
| storeId | long | 门店 ID |
| diningMethod | integer | 用餐方式：`1` 堂食，`2` 外带 |
| source | integer | 订单来源：`0` 线下，`1` 支付宝，`2` 微信，`3` APP |
| note | string | 订单备注 |
| pickupCode | string | 取餐码 |
| makingStatus | integer | 制作状态：`0` 待制作，`1` 制作中，`2` 制作完成，`3` 已取餐，`4` 已取消 |
| paymentMethod | integer | 支付方式：`0` 现金，`1` 支付宝，`2` 微信 |
| paymentStatus | integer | 支付状态：`0` 支付中，`1` 支付超时，`2` 已支付，`3` 退款中，`4` 已退款 |
| paymentExpiredAt | string | 支付过期时间，格式 `yyyy-MM-ddTHH:mm:ss` |
| paymentPaidAt | string | 支付完成时间 |
| paymentRefundedAt | string | 退款时间 |
| createdAt | string | 下单时间 |
| updatedAt | string | 更新时间 |
| totalPrice | decimal | 订单总价（元），保留两位小数 |
| totalQuantity | integer | 订单商品总数量 |
| items | array | 订单项列表，见下方「items 数组元素」 |

### items 数组元素

| 参数 | 类型 | 说明 |
| ---- | ---- | ---- |
| id | long | 订单项 ID |
| productId | long | 商品 ID |
| productName | string | 商品名称 |
| skuId | string | 商品 SKU 编码 |
| customization | string | 客制化原始值，未做格式转换 |
| coverUrl | string | 商品封面图 URL |
| quantity | integer | 数量 |
| unitPrice | decimal | 单价（元） |
| totalPrice | decimal | 小计（元） |
| available | boolean | 是否可售 |

## 响应示例

```json
{
  "code": 0,
  "message": "成功",
  "data": {
    "id": 1,
    "orderNo": "DX202604230001",
    "tradeNo": "202604231234567890",
    "storeId": 1,
    "diningMethod": 2,
    "source": 1,
    "note": "少冰",
    "pickupCode": "A12",
    "makingStatus": 1,
    "paymentMethod": 1,
    "paymentStatus": 2,
    "paymentExpiredAt": "2026-04-23T15:45:00",
    "paymentPaidAt": "2026-04-23T15:30:00",
    "paymentRefundedAt": null,
    "createdAt": "2026-04-23T15:15:00",
    "updatedAt": "2026-04-23T15:30:00",
    "totalPrice": 36.00,
    "totalQuantity": 2,
    "items": [
      {
        "id": 1,
        "productId": 1001,
        "productName": "生椰拿铁",
        "skuId": "1001#1_2",
        "customization": "温度_热-糖度_少糖",
        "coverUrl": "https://example.com/a.jpg",
        "quantity": 2,
        "unitPrice": 18.00,
        "totalPrice": 36.00,
        "available": true
      }
    ]
  }
}
```

## 错误码字典

| 错误码 | HTTP 状态码 | 说明 |
| ------ | ----------- | ---- |
| 50000 | 500 | 系统繁忙，请稍后重试 |
| 50101 | 500 | 数据库访问异常（系统繁忙，请稍后重试） |
| 21016 | 400 | 订单不存在 |
| 21019 | 400 | 该订单不属于当前门店 |
| 40001 | 400 | 缺少请求头 |
| 40002 | 400 | 参数缺失 / 参数类型不正确 |
| 40100 | 401 | 未登录 |
