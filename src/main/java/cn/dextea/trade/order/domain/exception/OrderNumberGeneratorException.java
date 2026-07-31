package cn.dextea.trade.order.domain.exception;

public class OrderNumberGeneratorException extends RuntimeException {
    public OrderNumberGeneratorException(String message) {
        super(message);
    }

    public OrderNumberGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }
}
