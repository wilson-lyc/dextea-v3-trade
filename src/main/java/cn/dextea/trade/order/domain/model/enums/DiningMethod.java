package cn.dextea.trade.order.domain.model.enums;

public enum DiningMethod implements cn.dextea.trade.shared.domain.enumeration.CodeEnum {
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
}
