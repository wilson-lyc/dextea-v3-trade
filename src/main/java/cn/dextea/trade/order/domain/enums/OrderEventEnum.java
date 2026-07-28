package cn.dextea.trade.order.domain.enums;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
@Getter
@RequiredArgsConstructor
public enum OrderEventEnum {
    PAY("支付成功"),
    PAY_TIMEOUT("超时未支付关闭"),
    REFUND("全额退款");
    private final String description;
}
