package cn.dextea.trade.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 支付方式枚举，对应 {@code orders.pay_method} 字段（tinyint，可空）。
 */
@Getter
@RequiredArgsConstructor
public enum PayMethod {

    /** 未指定 */
    NONE(0, "未指定"),
    /** 微信支付 */
    WECHAT(1, "微信支付"),
    /** 支付宝 */
    ALIPAY(2, "支付宝"),
    /** 银行卡 */
    BANK_CARD(3, "银行卡");

    private final int code;
    private final String description;

    public static PayMethod of(Integer code) {
        if (code == null) {
            return null;
        }
        for (PayMethod method : values()) {
            if (method.code == code) {
                return method;
            }
        }
        throw new IllegalArgumentException("未知支付方式: " + code);
    }
}
