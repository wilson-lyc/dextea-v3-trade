package cn.dextea.trade.order.domain.enums;
import cn.dextea.trade.common.enums.CodeEnum;
import cn.dextea.trade.common.enums.EnumUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
@Getter
@RequiredArgsConstructor
public enum TradeStatusEnum implements CodeEnum {
    TRADE_WAIT_PAY(0, "待支付"),
    TRADE_PAID(1, "已支付"),
    TRADE_PAY_TIMEOUT(2, "支付超时"),
    TRADE_REFUNDING(3, "退款中"),
    TRADE_REFUNDED(4, "已退款");
    private final int code;
    private final String description;
    public static TradeStatusEnum of(Integer code) {
        return EnumUtils.of(TradeStatusEnum.class, code);
    }
}
