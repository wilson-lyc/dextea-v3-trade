package cn.dextea.trade.error;

import cn.dextea.trade.common.BizErrorCode;

public enum OrderErrorCode implements BizErrorCode {

    PRODUCT_NOT_FOUND(1002, "商品ID错误"),
    SKU_INVALID(1003, "skuId 格式非法"),
    CUSTOMIZATION_OPTION_NOT_FOUND(1004, "客制化选项ID错误"),
    CUSTOMIZATION_NOT_FOUND(1005, "客制化项目ID错误"),
    STORE_ID_INVALID(1006, "门店ID错误"),
    CUSTOMER_ID_INVALID(1007, "顾客ID错误"),
    ORDER_CREATE_FAILED(1009, "订单创建失败"),
    ALIPAY_BUYER_NOT_BOUND(1010, "顾客未绑定支付宝，无法创建支付"),
    ALIPAY_TRADE_CREATE_FAILED(1011, "支付宝创建交易失败");

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
