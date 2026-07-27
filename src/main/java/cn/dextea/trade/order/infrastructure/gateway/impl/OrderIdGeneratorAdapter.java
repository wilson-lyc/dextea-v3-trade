package cn.dextea.trade.order.infrastructure.gateway.impl;

import cn.dextea.trade.order.domain.gateway.OrderIdGeneratorGateway;
import lombok.RequiredArgsConstructor;
import me.ahoo.cosid.provider.IdGeneratorProvider;
import org.springframework.stereotype.Component;

/**
 * 订单号生成适配器：实现 {@link OrderIdGeneratorGateway}，委派 cosid 命名生成器 {@code order}。
 */
@Component
@RequiredArgsConstructor
public class OrderIdGeneratorAdapter implements OrderIdGeneratorGateway {

    /**
     * 订单号生成器名称，对应 cosid.snowflake.provider.order。
     */
    private static final String ORDER_ID_GENERATOR = "order";

    private final IdGeneratorProvider idGeneratorProvider;

    @Override
    public String generateOrderNo() {
        return String.valueOf(idGeneratorProvider.getRequired(ORDER_ID_GENERATOR).generate());
    }
}
