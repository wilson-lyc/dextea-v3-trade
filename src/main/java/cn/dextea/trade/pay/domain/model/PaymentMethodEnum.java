package cn.dextea.trade.pay.domain.model;

import cn.dextea.trade.enums.CodeEnum;
import cn.dextea.trade.enums.EnumUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 支付方式枚举（支付域领域模型）。
 */
@Getter
@RequiredArgsConstructor
public enum PaymentMethodEnum implements CodeEnum {

    WECHAT(1, "微信支付"),
    ALIPAY(2, "支付宝");

    private final int code;
    private final String description;

    public static PaymentMethodEnum of(Integer code) {
        return EnumUtils.of(PaymentMethodEnum.class, code);
    }
}
