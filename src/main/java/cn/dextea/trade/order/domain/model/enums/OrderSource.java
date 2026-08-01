package cn.dextea.trade.order.domain.model.enums;

public enum OrderSource implements cn.dextea.trade.shared.domain.enumeration.CodeEnum {
    OFFLINE(0, "线下"),
    ALIPAY(1, "支付宝"),
    WEIXIN(2, "微信"),
    APP(3, "APP");

    private final int code;
    private final String description;

    OrderSource(int code, String description) {
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
