package cn.dextea.trade.order.domain.exception;

public class PickupCodeGeneratorException extends RuntimeException {
    public PickupCodeGeneratorException(String message) {
        super(message);
    }

    public PickupCodeGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }
}
