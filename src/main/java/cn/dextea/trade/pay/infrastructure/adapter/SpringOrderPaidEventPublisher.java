package cn.dextea.trade.pay.infrastructure.adapter;

import cn.dextea.trade.pay.domain.port.OrderPaidEventPublisher;
import cn.dextea.trade.shared.event.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringOrderPaidEventPublisher implements OrderPaidEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(OrderPaidEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
