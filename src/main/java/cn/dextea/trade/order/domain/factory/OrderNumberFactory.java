package cn.dextea.trade.order.domain.factory;

import cn.dextea.trade.order.domain.model.valueobject.OrderNumber;
import cn.dextea.trade.order.domain.port.OrderNumberGenerator;

import java.util.Objects;

public class OrderNumberFactory {

    private final OrderNumberGenerator generator;

    public OrderNumberFactory(OrderNumberGenerator generator) {
        this.generator = Objects.requireNonNull(generator, "订单号生成器不能为空");
    }

    public OrderNumber create() {
        return OrderNumber.of(generator.next());
    }
}
