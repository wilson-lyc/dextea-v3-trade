package cn.dextea.trade.order.domain.model.enums;

public enum PaymentMethod implements cn.dextea.trade.shared.domain.enumeration.CodeEnum {
    CASH(0, "现金"),
    ALIPAY(1, "支付宝"),
    WEIXIN(2, "微信");

    private final int code;
    private final String description;

    PaymentMethod(int code, String description) {
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
