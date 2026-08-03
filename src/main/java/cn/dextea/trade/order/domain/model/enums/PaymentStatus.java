package cn.dextea.trade.order.domain.model.enums;

import cn.dextea.trade.shared.domain.enumeration.CodeEnum;
import cn.dextea.trade.shared.domain.enumeration.EnumUtils;

public enum PaymentStatus implements CodeEnum {
    PENDING(0, "支付中"),
    TIMEOUT(1, "支付超时"),
    PAID(2, "已支付"),
    REFUNDING(3, "退款中"),
    REFUNDED(4, "已退款");

    private final int code;
    private final String description;

    PaymentStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static PaymentStatus of(Integer code) {
        return EnumUtils.of(PaymentStatus.class, code);
    }
}
