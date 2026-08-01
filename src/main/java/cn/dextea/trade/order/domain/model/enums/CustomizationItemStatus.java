package cn.dextea.trade.order.domain.model.enums;

public enum CustomizationItemStatus {
    DISABLED(0, "禁用"),
    ACTIVE(1, "激活");

    private final int code;
    private final String description;

    CustomizationItemStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
