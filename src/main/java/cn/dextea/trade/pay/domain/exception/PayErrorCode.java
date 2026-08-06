package cn.dextea.trade.pay.domain.exception;

import cn.dextea.trade.shared.error.BizErrorCode;

public enum PayErrorCode implements BizErrorCode {
    ALIPAY_CREATE_TRADE_FAILED(102001, "支付宝创建交易失败"),
    ALIPAY_CONFIG_MISSING(102002, "支付宝配置缺失"),
    UNSUPPORTED_PAYMENT_METHOD(102003, "不支持的支付方式"),
    ALIPAY_QUERY_TRADE_FAILED(102004, "支付宝交易查询失败"),
    PAY_CALLBACK_MESSAGE_INVALID(102005, "支付回调消息非法");

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
