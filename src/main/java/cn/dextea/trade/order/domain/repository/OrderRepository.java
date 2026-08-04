package cn.dextea.trade.order.domain.repository;

import cn.dextea.trade.order.domain.model.Order;

import java.util.List;

public interface OrderRepository {
    Order save(Order order);

    Order getById(Long orderId);

    Order findByOrderNo(String orderNo);

    List<Order> getMonthOrders(Long customerId, int year, int month);

    void updatePaymentStatus(Order order);
}
