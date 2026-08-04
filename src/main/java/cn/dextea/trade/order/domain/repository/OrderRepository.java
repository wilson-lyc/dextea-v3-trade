package cn.dextea.trade.order.domain.repository;

import cn.dextea.trade.order.domain.model.Order;

import java.util.List;

public interface OrderRepository {
    Order save(Order order);

    List<Order> getMonthOrders(Long customerId, int year, int month);

    Order getById(Long orderId);
}
