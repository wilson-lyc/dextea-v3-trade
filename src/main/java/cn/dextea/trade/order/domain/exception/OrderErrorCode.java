package cn.dextea.trade.order.domain.exception;

import cn.dextea.trade.common.error.BizErrorCode;

public enum OrderErrorCode implements BizErrorCode {

    PRODUCT_NOT_FOUND(1002, "商品ID错误"),
    SKU_INVALID(1003, "skuId 格式非法"),
    CUSTOMIZATION_OPTION_NOT_FOUND(1004, "客制化选项ID错误"),
    CUSTOMIZATION_NOT_FOUND(1005, "客制化项目ID错误"),
    CUSTOMIZATION_BINDING_INVALID(1026, "客制化绑定关系非法"),
    STORE_NOT_OPEN(1014, "门店未营业，无法下单"),
    CUSTOMER_NOT_ACTIVE(1015, "顾客未激活，无法下单"),
    ORDER_CREATE_FAILED(1009, "订单创建失败"),
    PAY_PLATFORM_NOT_SUPPORTED(1013, "暂不支持的支付方式"),
    ALIPAY_BUYER_NOT_BOUND(1010, "顾客未绑定支付宝，无法创建支付"),
    DINING_METHOD_INVALID(1012, "用餐方式错误"),
    ORDER_NOT_FOUND(1016, "订单不存在"),
    ORDER_ACCESS_DENIED(1017, "订单不属于该顾客"),
    ORDER_STATUS_TRANSITION_INVALID(1018, "订单状态流转非法"),
    ORDER_STATUS_CAS_FAILED(1019, "订单状态已变更，请刷新后重试"),
    ORDER_LOCK_BUSY(1020, "系统繁忙，请稍后重试"),
    ORDER_ITEMS_EMPTY(1021, "订单明细不能为空"),
    ORDER_PRICE_INVALID(1022, "订单金额非法"),
    ORDER_QUANTITY_INVALID(1023, "订单数量非法"),
    ORDER_TRADE_NO_ALREADY_SET(1024, "trade_no 已存在，不可重复设置"),
    ORDER_PAY_EXPIRE_AT_INVALID(1025, "支付过期时间非法"),
    ORDER_DUPLICATE_REQUEST(1027, "重复请求，请勿重复下单");

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
