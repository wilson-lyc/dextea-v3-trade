package cn.dextea.trade.order.domain.factory;

import cn.dextea.trade.common.error.BizError;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.exception.OrderNumberGeneratorException;
import cn.dextea.trade.order.domain.model.valueobject.OrderNumber;
import cn.dextea.trade.order.domain.port.OrderNumberGenerator;

import java.util.Objects;

public class OrderNumberFactory {

    private final OrderNumberGenerator generator;

    public OrderNumberFactory(OrderNumberGenerator generator) {
        this.generator = Objects.requireNonNull(generator, "订单号生成器不能为空");
    }

    public OrderNumber create() {
        try {
            return OrderNumber.of(generator.next());
        } catch (OrderNumberGeneratorException e) {
            throw new BizError(OrderErrorCode.ORDER_NO_GENERATE_FAILED, e.getMessage(), e);
        }
    }
}
