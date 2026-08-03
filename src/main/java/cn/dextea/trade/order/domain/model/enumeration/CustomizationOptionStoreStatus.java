package cn.dextea.trade.order.domain.model.enumeration;

import cn.dextea.trade.shared.domain.enumeration.CodeEnum;
import cn.dextea.trade.shared.domain.enumeration.EnumUtils;

public enum CustomizationOptionStoreStatus implements CodeEnum {
    DISABLED(0, "禁用"),
    ACTIVE(1, "激活");

    private final int code;
    private final String description;

    CustomizationOptionStoreStatus(int code, String description) {
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

    public static CustomizationOptionStoreStatus of(Integer code) {
        return EnumUtils.of(CustomizationOptionStoreStatus.class, code);
    }
}
