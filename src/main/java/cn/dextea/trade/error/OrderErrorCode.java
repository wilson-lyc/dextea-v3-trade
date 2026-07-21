package cn.dextea.trade.error;

import cn.dextea.trade.common.BizErrorCode;

public enum OrderErrorCode implements BizErrorCode {

    PRODUCT_UNAVAILABLE(1001, "商品已下架，不可购买"),
    PRODUCT_NOT_FOUND(1002, "商品ID错误"),
    SKU_INVALID(1003, "skuId 格式非法"),
    CUSTOMIZATION_OPTION_NOT_FOUND(1004, "客制化选项ID错误"),
    CUSTOMIZATION_NOT_FOUND(1005, "客制化项目ID错误"),
    STORE_ID_INVALID(1006, "门店ID错误"),
    CUSTOMER_ID_INVALID(1007, "顾客ID错误");

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
