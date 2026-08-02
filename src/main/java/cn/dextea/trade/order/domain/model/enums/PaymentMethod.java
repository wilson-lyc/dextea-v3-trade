package cn.dextea.trade.order.domain.model.enums;

import cn.dextea.trade.shared.domain.enumeration.CodeEnum;
import cn.dextea.trade.shared.domain.enumeration.EnumUtils;

public enum PaymentMethod implements CodeEnum {
    CASH(0, "现金"),
    ALIPAY(1, "支付宝"),
    WEIXIN(2, "微信");

    private final int code;
    private final String description;

    PaymentMethod(int code, String description) {
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

    public static PaymentMethod of(Integer code) {
        return EnumUtils.of(PaymentMethod.class, code);
    }
}
