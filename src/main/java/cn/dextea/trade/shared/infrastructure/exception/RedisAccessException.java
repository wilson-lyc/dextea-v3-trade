package cn.dextea.trade.shared.infrastructure.exception;

public class RedisAccessException extends RuntimeException {
    public RedisAccessException(String message) {
        super(message);
    }

    public RedisAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
