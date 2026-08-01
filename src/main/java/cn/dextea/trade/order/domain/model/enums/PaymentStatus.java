package cn.dextea.trade.order.domain.model.enums;

public enum PaymentStatus {
    PENDING(0, "支付中"),
    TIMEOUT(1, "支付超时")
    PAID(2, "已支付"),
    REFUNDING(3, "退款中"),
    REFUNDED(4, "已退款");

    private final int code;
    private final String description;

    PaymentStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
