package cn.dextea.trade.order.domain.port;

import cn.dextea.trade.order.domain.model.Order;

public interface OrderTimeoutDelayPort {
    void scheduleTimeout(Order order);
}
