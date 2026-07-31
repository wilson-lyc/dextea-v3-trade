package cn.dextea.trade.order.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Map;

@Getter
@EqualsAndHashCode
public final class PaymentMethod {

    public static final PaymentMethod CASH = new PaymentMethod(0, "现金");
    public static final PaymentMethod ALIPAY = new PaymentMethod(1, "支付宝");
    public static final PaymentMethod WEIXIN = new PaymentMethod(2, "微信");

    private static final Map<Integer, PaymentMethod> CACHE = Map.of(
            0, CASH,
            1, ALIPAY,
            2, WEIXIN
    );

    private final int code;
    private final String description;

    private PaymentMethod(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static PaymentMethod of(int code) {
        PaymentMethod method = CACHE.get(code);
        if (method == null) {
            throw new IllegalArgumentException("非法的支付方式枚举值: " + code);
        }
        return method;
    }

    @Override
    public String toString() {
        return Integer.toString(code);
    }
}
