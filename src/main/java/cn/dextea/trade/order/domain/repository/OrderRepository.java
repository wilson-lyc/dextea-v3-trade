package cn.dextea.trade.order.domain.repository;

import cn.dextea.trade.order.domain.model.Order;

public interface OrderRepository {
    Order save(Order order);
}
