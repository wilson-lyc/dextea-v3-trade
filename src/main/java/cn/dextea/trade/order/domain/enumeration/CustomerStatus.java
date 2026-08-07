package cn.dextea.trade.order.domain.enumeration;

import cn.dextea.trade.shared.enumeration.CodeEnum;
import cn.dextea.trade.shared.enumeration.EnumUtils;

public enum CustomerStatus implements CodeEnum {
    DISABLED(0, "禁用"),
    ACTIVE(1, "激活");

    private final int code;
    private final String description;

    CustomerStatus(int code, String description) {
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

    public static CustomerStatus of(Integer code) {
        return EnumUtils.of(CustomerStatus.class, code);
    }
}
