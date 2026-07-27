package cn.dextea.trade.pay.domain.exception;

import cn.dextea.trade.common.error.BizErrorCode;

/**
 * 支付域错误码
 */
public enum PayErrorCode implements BizErrorCode {

    ALIPAY_TRADE_CREATE_FAILED(1011, "支付宝创建交易失败");

    private final int code;

    private final String message;

    PayErrorCode(int code, String message) {
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
