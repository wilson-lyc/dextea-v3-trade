package cn.dextea.trade.order.domain.exception;

import cn.dextea.trade.common.BizErrorCode;

public enum OrderErrorCode implements BizErrorCode {

    PRODUCT_NOT_FOUND(1002, "商品ID错误"),
    SKU_INVALID(1003, "skuId 格式非法"),
    CUSTOMIZATION_OPTION_NOT_FOUND(1004, "客制化选项ID错误"),
    CUSTOMIZATION_NOT_FOUND(1005, "客制化项目ID错误"),
    STORE_ID_INVALID(1006, "门店ID错误"),
    CUSTOMER_ID_INVALID(1007, "顾客ID错误"),
    STORE_NOT_OPEN(1014, "门店未营业，无法下单"),
    CUSTOMER_NOT_ACTIVE(1015, "顾客未激活，无法下单"),
    ORDER_CREATE_FAILED(1009, "订单创建失败"),
    DINING_METHOD_INVALID(1012, "用餐方式错误"),
    ORDER_NOT_FOUND(1016, "订单不存在"),
    ORDER_ACCESS_DENIED(1017, "订单不属于该顾客"),
    ORDER_STATUS_TRANSITION_INVALID(1018, "订单状态流转非法"),
    ORDER_STATUS_CAS_FAILED(1019, "订单状态已变更，请刷新后重试"),
    ORDER_LOCK_BUSY(1020, "系统繁忙，请稍后重试");

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
