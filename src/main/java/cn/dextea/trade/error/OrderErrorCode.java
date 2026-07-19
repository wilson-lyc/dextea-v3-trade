package cn.dextea.trade.error;

import cn.dextea.trade.common.BizErrorCode;

public enum OrderErrorCode implements BizErrorCode {

    PRODUCT_UNAVAILABLE(1001, "商品已下架，不可购买"),
    PRODUCT_NOT_FOUND(1002, "商品不存在");

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
