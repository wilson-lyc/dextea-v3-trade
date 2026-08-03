package cn.dextea.trade.shared.domain.enumeration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod implements StringCodeEnum, CodeEnum {
    @JsonProperty("cash")
    CASH("cash", 0, "现金"),
    @JsonProperty("alipay")
    ALIPAY("alipay", 1, "支付宝"),
    @JsonProperty("weixin")
    WEIXIN("weixin", 2, "微信");

    private final String key;
    private final int code;
    private final String description;

    @Override
    public String getValue() {
        return key;
    }

    public static PaymentMethod of(String value) {
        return EnumUtils.of(PaymentMethod.class, value);
    }

    public static PaymentMethod of(Integer code) {
        return EnumUtils.of(PaymentMethod.class, code);
    }
}
