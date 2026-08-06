package cn.dextea.trade.order.domain.model.enumeration;

import cn.dextea.trade.shared.enumeration.CodeEnum;
import cn.dextea.trade.shared.enumeration.EnumUtils;

public enum DiningMethod implements CodeEnum {
    DINE_IN(1, "堂食"),
    TAKEOUT(2, "外带");

    private final int code;
    private final String description;

    DiningMethod(int code, String description) {
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

    public static DiningMethod of(Integer code) {
        return EnumUtils.of(DiningMethod.class, code);
    }
}
