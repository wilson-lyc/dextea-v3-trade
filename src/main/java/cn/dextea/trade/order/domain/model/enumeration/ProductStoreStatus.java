package cn.dextea.trade.order.domain.model.enumeration;

import cn.dextea.trade.shared.domain.enumeration.CodeEnum;
import cn.dextea.trade.shared.domain.enumeration.EnumUtils;

public enum ProductStoreStatus implements CodeEnum {
    SOLD_OUT(0, "售罄"),
    ACTIVE(1, "可售");

    private final int code;
    private final String description;

    ProductStoreStatus(int code, String description) {
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

    public static ProductStoreStatus of(Integer code) {
        return EnumUtils.of(ProductStoreStatus.class, code);
    }
}
