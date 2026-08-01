package cn.dextea.trade.order.domain.model.enums;

public enum ProductGlobalStatus {
    DISABLED(0, "售罄"),
    ACTIVE(1, "可售");

    private final int code;
    private final String description;

    ProductGlobalStatus(int code, String description) {
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
