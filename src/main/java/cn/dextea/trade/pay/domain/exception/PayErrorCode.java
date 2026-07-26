package cn.dextea.trade.pay.domain.exception;

import cn.dextea.trade.common.BizErrorCode;

/**
 * 支付域错误码
 */
public enum PayErrorCode implements BizErrorCode {

    ALIPAY_BUYER_NOT_BOUND(1010, "顾客未绑定支付宝，无法创建支付"),
    ALIPAY_TRADE_CREATE_FAILED(1011, "支付宝创建交易失败"),
    PAY_PLATFORM_NOT_SUPPORTED(1013, "暂不支持的支付方式");

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
