package cn.dextea.trade.order.domain.factory;

import cn.dextea.trade.order.domain.model.valueobject.PickupCode;
import cn.dextea.trade.order.domain.port.PickupCodeGenerator;

import java.util.Objects;

public class PickupCodeFactory {

    private final PickupCodeGenerator generator;

    public PickupCodeFactory(PickupCodeGenerator generator) {
        this.generator = Objects.requireNonNull(generator, "取餐码生成器不能为空");
    }

    public PickupCode create(Long storeId) {
        return PickupCode.of(generator.next(storeId));
    }
}
