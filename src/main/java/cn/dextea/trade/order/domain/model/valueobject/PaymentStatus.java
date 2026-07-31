package cn.dextea.trade.order.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Map;

@Getter
@EqualsAndHashCode
public final class PaymentStatus {

    public static final PaymentStatus PENDING = new PaymentStatus(0, "支付中");
    public static final PaymentStatus PAID = new PaymentStatus(1, "已支付");
    public static final PaymentStatus REFUNDING = new PaymentStatus(2, "退款中");
    public static final PaymentStatus REFUNDED = new PaymentStatus(3, "已退款");
    public static final PaymentStatus PAY_TIMEOUT = new PaymentStatus(4, "支付超时");

    private static final Map<Integer, PaymentStatus> CACHE = Map.of(
            0, PENDING,
            1, PAID,
            2, REFUNDING,
            3, REFUNDED,
            4, PAY_TIMEOUT
    );

    private final int code;
    private final String description;

    private PaymentStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static PaymentStatus of(int code) {
        PaymentStatus status = CACHE.get(code);
        if (status == null) {
            throw new IllegalArgumentException("非法的支付状态枚举值: " + code);
        }
        return status;
    }

    @Override
    public String toString() {
        return Integer.toString(code);
    }
}
