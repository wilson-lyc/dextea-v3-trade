package cn.dextea.trade.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PayMethodEnum {

    WECHAT(1, "微信支付"),
    ALIPAY(2, "支付宝");

    private final int code;
    private final String description;

    public static PayMethodEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        for (PayMethodEnum method : values()) {
            if (method.code == code) {
                return method;
            }
        }
        throw new IllegalArgumentException("未知支付方式: " + code);
    }
}
