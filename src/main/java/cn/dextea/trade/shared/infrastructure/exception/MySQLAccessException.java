package cn.dextea.trade.shared.infrastructure.exception;

public class MySQLAccessException extends RuntimeException {
    public MySQLAccessException(String message) {
        super(message);
    }

    public MySQLAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
