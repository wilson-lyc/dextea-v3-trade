package cn.dextea.trade.order.domain.factory;

import cn.dextea.trade.common.error.BizError;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.valueobject.PickupCode;
import cn.dextea.trade.order.domain.exception.PickupCodeGeneratorException;
import cn.dextea.trade.order.domain.port.PickupCodeGenerator;

import java.util.Objects;

public class PickupCodeFactory {

    private final PickupCodeGenerator generator;

    public PickupCodeFactory(PickupCodeGenerator generator) {
        this.generator = Objects.requireNonNull(generator, "取餐码生成器不能为空");
    }

    public PickupCode create(Long storeId) {
        try {
            return PickupCode.of(generator.next(storeId));
        } catch (PickupCodeGeneratorException e) {
            throw new BizError(OrderErrorCode.PICKUP_CODE_GENERATE_FAILED, e.getMessage(), e);
        }
    }
}
