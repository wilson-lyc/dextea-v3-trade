# 查询门店制作看板

## 接口说明

查询门店当前制作现场的实时概况，供出餐屏/叫号屏轮询展示。返回四类数据：

- `preparingPickupCodes`：制作中（`makingStatus = 1`）订单的取餐码列表。
- `readyPickupCodes`：制作完成（`makingStatus = 2`）但尚未取餐的订单取餐码列表。
- `preparingOrderCount`：当前制作中的订单总数。
- `preparingProductQuantity`：当前制作中的商品总数量，为所有制作中订单 `totalQuantity` 之和。

仅统计归属本门店（`X-Store-Id`）的订单，且只覆盖「制作中」与「制作完成」两种制作状态——待制作、已取餐、已取消的订单不在本看板范围内。列表中同一取餐码可能出现多次（单店单日订单超过 1000 笔时取餐码会取模回绕），业务上以订单号区分主体。列表按订单下单时间正序排列（先下单的排在前）。

## 请求地址

```
GET /api/v1/store/orders/making-board
```

## 请求头

| 请求头 | 必填 | 说明 |
| ------ | ---- | ---- |
| Authorization | 是 | API 访问令牌，格式 `Bearer {token}` |
| X-Store-Id | 是 | 门店 ID，标识当前门店 |

## 请求参数

无查询参数。

无 Body 参数。

## 请求示例

```bash
curl "http://localhost:8080/api/v1/store/orders/making-board" \
  -H "Authorization: Bearer {token}" \
  -H "X-Store-Id: 1"
```

## 响应格式

`data` 字段：

| 参数 | 类型 | 说明 |
| ---- | ---- | ---- |
| preparingPickupCodes | array | 制作中订单的取餐码列表，元素为 string，按下单时间正序；无数据时为空数组 `[]` |
| readyPickupCodes | array | 制作完成待取餐订单的取餐码列表，元素为 string，按下单时间正序；无数据时为空数组 `[]` |
| preparingOrderCount | long | 当前制作中的订单总数，无数据时为 `0` |
| preparingProductQuantity | integer | 当前制作中的商品总数量，为所有制作中订单商品数量之和，无数据时为 `0` |

## 响应示例

```json
{
  "code": 0,
  "message": "成功",
  "data": {
    "preparingPickupCodes": ["8003", "8006", "8007"],
    "readyPickupCodes": ["8001", "8002"],
    "preparingOrderCount": 3,
    "preparingProductQuantity": 7
  }
}
```

## 错误码字典

| 错误码 | HTTP 状态码 | 说明 |
| ------ | ----------- | ---- |
| 50000 | 500 | 系统繁忙，请稍后重试 |
| 50101 | 500 | 数据库访问异常（系统繁忙，请稍后重试） |
| 40001 | 400 | 缺少请求头 |
| 40002 | 400 | 参数缺失 / 参数类型不正确 |
| 40100 | 401 | 未登录 |
