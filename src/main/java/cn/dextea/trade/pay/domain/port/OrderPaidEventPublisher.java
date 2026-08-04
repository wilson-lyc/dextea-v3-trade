package cn.dextea.trade.pay.domain.port;

import cn.dextea.trade.shared.domain.event.OrderPaidEvent;

public interface OrderPaidEventPublisher {

    void publish(OrderPaidEvent event);
}
