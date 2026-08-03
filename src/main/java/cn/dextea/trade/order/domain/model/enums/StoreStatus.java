package cn.dextea.trade.order.domain.model.enums;

import cn.dextea.trade.shared.domain.enumeration.CodeEnum;
import cn.dextea.trade.shared.domain.enumeration.EnumUtils;

public enum StoreStatus implements CodeEnum {
    CLOSED(0, "休息中"),
    OPEN(1, "营业中"),
    PENDING(2, "筹备中"),
    DEFUNCT(3, "已注销");

    private final int code;
    private final String description;

    StoreStatus(int code, String description) {
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

    public static StoreStatus of(Integer code) {
        return EnumUtils.of(StoreStatus.class, code);
    }
}
