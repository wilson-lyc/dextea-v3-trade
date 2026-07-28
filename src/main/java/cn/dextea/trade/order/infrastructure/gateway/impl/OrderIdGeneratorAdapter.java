package cn.dextea.trade.order.infrastructure.gateway.impl;
import cn.dextea.trade.order.domain.gateway.OrderIdGeneratorGateway;
import lombok.RequiredArgsConstructor;
import me.ahoo.cosid.provider.IdGeneratorProvider;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
public class OrderIdGeneratorAdapter implements OrderIdGeneratorGateway {
    private static final String ORDER_ID_GENERATOR = "order";
    private final IdGeneratorProvider idGeneratorProvider;
    @Override
    public String generateOrderNo() {
        return String.valueOf(idGeneratorProvider.getRequired(ORDER_ID_GENERATOR).generate());
    }
}
