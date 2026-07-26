package cn.dextea.trade.pay.domain.model;

import cn.dextea.trade.enums.EnumUtils;
import cn.dextea.trade.enums.StringCodeEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 支付渠道枚举（支付域领域模型）。
 */
@Getter
@RequiredArgsConstructor
public enum PlatformEnum implements StringCodeEnum {

    @JsonProperty("weixin")
    WEIXIN("weixin", PaymentMethodEnum.WECHAT),

    @JsonProperty("alipay")
    ALIPAY("alipay", PaymentMethodEnum.ALIPAY);

    private final String value;
    private final PaymentMethodEnum payMethod;

    public static PlatformEnum of(String value) {
        return EnumUtils.of(PlatformEnum.class, value);
    }
}
