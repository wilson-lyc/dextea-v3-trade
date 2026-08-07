package cn.dextea.trade.order.domain.repository;

import cn.dextea.trade.order.domain.model.Order;

import java.util.List;

public interface OrderRepository {
    Order save(Order order);

    Order getOrderById(Long orderId);

    Order getSummaryById(Long orderId);

    Order getSummaryByOrderNo(String orderNo);

    List<Order> getMonthOrders(Long customerId, int year, int month);

    void updatePaymentStatus(Order order);

    void cancelOrder(Order order);

    void updateMakingStatus(Order order);
}
