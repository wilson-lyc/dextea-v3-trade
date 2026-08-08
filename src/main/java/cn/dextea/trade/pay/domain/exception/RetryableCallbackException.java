package cn.dextea.trade.pay.domain.exception;

import cn.dextea.trade.shared.error.RetryableException;

public class RetryableCallbackException extends RetryableException {

    public RetryableCallbackException(String message) {
        super(message);
    }

    public RetryableCallbackException(String message, Throwable cause) {
        super(message, cause);
    }
}
