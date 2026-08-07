package cn.dextea.trade.order.domain.exception;

public class RetryableOrderException extends RuntimeException {
    public RetryableOrderException(String message) {
        super(message);
    }

    public RetryableOrderException(String message, Throwable cause) {
        super(message, cause);
    }
}
