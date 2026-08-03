package cn.dextea.trade.order.domain.model.enums;

import cn.dextea.trade.shared.domain.enumeration.CodeEnum;
import cn.dextea.trade.shared.domain.enumeration.EnumUtils;

public enum ProductGlobalStatus implements CodeEnum {
    DISABLED(0, "下架"),
    ACTIVE(1, "上架");

    private final int code;
    private final String description;

    ProductGlobalStatus(int code, String description) {
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

    public static ProductGlobalStatus of(Integer code) {
        return EnumUtils.of(ProductGlobalStatus.class, code);
    }
}
