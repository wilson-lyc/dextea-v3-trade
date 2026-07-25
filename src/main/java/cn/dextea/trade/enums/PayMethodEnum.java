package cn.dextea.trade.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PayMethodEnum implements CodeEnum {

    WECHAT(1, "微信支付"),
    ALIPAY(2, "支付宝");

    private final int code;
    private final String description;

    public static PayMethodEnum of(Integer code) {
        return EnumUtils.of(PayMethodEnum.class, code);
    }
}
