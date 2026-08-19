package cn.dextea.trade.payment.domain.port;

import cn.dextea.trade.shared.event.OrderPaidEvent;

public interface OrderPaidEventPublisher {

    void publish(OrderPaidEvent event);
}
