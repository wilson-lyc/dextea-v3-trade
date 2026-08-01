package cn.dextea.trade.order.domain.model.enums;

public enum CustomerStatus implements cn.dextea.trade.shared.domain.enumeration.CodeEnum {
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
}
