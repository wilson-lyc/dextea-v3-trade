package cn.dextea.trade.order.infrastructure.adapter;

import cn.dextea.trade.order.domain.port.OrderIdGeneratorPort;
import lombok.RequiredArgsConstructor;
import me.ahoo.cosid.provider.IdGeneratorProvider;
import org.springframework.stereotype.Component;

/**
 * 订单号生成适配器：实现 {@link OrderIdGeneratorPort}，委派 cosid 命名生成器 {@code order}。
 */
@Component
@RequiredArgsConstructor
public class OrderIdGeneratorAdapter implements OrderIdGeneratorPort {

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
