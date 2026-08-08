package cn.dextea.trade.order.domain.exception;

import cn.dextea.trade.shared.error.RetryableException;

public class RetryableOrderException extends RetryableException {

    public RetryableOrderException(String message) {
        super(message);
    }

    public RetryableOrderException(String message, Throwable cause) {
        super(message, cause);
    }
}
