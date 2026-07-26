package cn.dextea.trade.order.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderEventEnum {

    PAY("支付成功"),
    PAY_AND_FINISH("支付并结算"),
    CLOSE("未付款关闭"),
    REFUND("全额退款");

    private final String description;
}
