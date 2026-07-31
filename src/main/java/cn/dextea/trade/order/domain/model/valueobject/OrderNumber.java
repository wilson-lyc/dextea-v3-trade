package cn.dextea.trade.order.domain.model.valueobject;

import cn.dextea.trade.common.error.BizError;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public final class OrderNumber {

    private static final int MAX_LENGTH = 64;

    private final String value;

    private OrderNumber(String value) {
        if (value == null || value.isBlank()) {
            throw new BizError(OrderErrorCode.ORDER_NO_EMPTY, "订单号不能为空");
        }
        if (value.length() > MAX_LENGTH) {
            throw new BizError(OrderErrorCode.ORDER_NO_LENGTH_EXCEED, "订单号长度不能超过 " + MAX_LENGTH);
        }
        this.value = value;
    }

    public static OrderNumber of(String value) {
        return new OrderNumber(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
