package cn.dextea.trade.order.domain.enums;

import cn.dextea.trade.common.enums.CodeEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TradeStatusEnum implements CodeEnum {

    TRADE_WAIT_PAY(0, "待支付"),
    TRADE_PAID(1, "已支付"),
    TRADE_FINISHED(2, "已结算"),
    TRADE_CLOSED(3, "已关闭"),
    TRADE_REFUNDING(4, "退款中"),
    TRADE_REFUNDED(5, "已退款");

    private final int code;
    private final String description;

    public static TradeStatusEnum of(Integer code) {
        return cn.dextea.trade.common.enums.EnumUtils.of(TradeStatusEnum.class, code);
    }
}
