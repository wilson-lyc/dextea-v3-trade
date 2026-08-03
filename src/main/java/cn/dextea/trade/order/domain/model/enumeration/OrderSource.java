package cn.dextea.trade.order.domain.model.enumeration;

import cn.dextea.trade.shared.domain.enumeration.CodeEnum;
import cn.dextea.trade.shared.domain.enumeration.EnumUtils;

public enum OrderSource implements CodeEnum {
    OFFLINE(0, "线下"),
    ALIPAY(1, "支付宝"),
    WEIXIN(2, "微信"),
    APP(3, "APP");

    private final int code;
    private final String description;

    OrderSource(int code, String description) {
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

    public static OrderSource of(Integer code) {
        return EnumUtils.of(OrderSource.class, code);
    }
}
