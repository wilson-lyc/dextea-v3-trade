package cn.dextea.trade.order.domain.exception;
import cn.dextea.trade.common.error.BizErrorCode;
public enum OrderErrorCode implements BizErrorCode {
    PRODUCT_ID_INVALID(101001, "商品ID非法"),
    SKU_INVALID(101002, "skuId 格式非法"),
    CUSTOMIZATION_OPTION_ID_INVALID(101003, "客制化选项ID非法"),
    CUSTOMIZATION_ID_INVALID(101004, "客制化项目ID非法"),
    CUSTOMIZATION_BINDING_INVALID(101005, "客制化绑定关系非法"),
    STORE_ID_INVALID(101006, "门店ID非法"),
    STORE_UNAVAILABLE(101007, "门店不可用，无法下单"),
    CUSTOMER_ID_INVALID(101008, "顾客ID非法"),
    CUSTOMER_UNAVAILABLE(101009, "顾客不可用，无法下单"),
    PAY_PLATFORM_NOT_SUPPORTED(101010, "暂不支持的支付方式"),
    ALIPAY_BUYER_NOT_BOUND(101011, "顾客未绑定支付宝，无法创建支付"),
    ORDER_PAY_EXPIRE_AT_INVALID(101012, "支付过期时间非法"),
    ORDER_CREATE_FAILED(101013, "订单创建失败"),
    DINING_METHOD_INVALID(101014, "用餐方式错误"),
    ORDER_NOT_FOUND(101015, "订单不存在"),
    ORDER_ACCESS_DENIED(101016, "订单不属于该顾客"),
    ORDER_STATUS_TRANSITION_INVALID(101017, "订单状态流转非法"),
    ORDER_STATUS_CAS_FAILED(101018, "订单状态已变更，请刷新后重试"),
    ORDER_LOCK_BUSY(101019, "系统繁忙，请稍后重试"),
    ORDER_ITEMS_EMPTY(101020, "订单明细不能为空"),
    ORDER_PRICE_INVALID(101021, "订单金额非法"),
    ORDER_QUANTITY_INVALID(101022, "订单数量非法"),
    ORDER_TRADE_NO_ALREADY_SET(101023, "trade_no 已存在，不可重复设置"),
    ORDER_DUPLICATE_REQUEST(101024, "重复请求，请勿重复下单"),
    ORDER_QUERY_MONTH_INVALID(101025, "查询年月非法");
    private final int code;
    private final String message;
    OrderErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    @Override
    public int getCode() {
        return code;
    }
    @Override
    public String getMessage() {
        return message;
    }
}
