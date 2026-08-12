package cn.dextea.trade.order.domain.repository;

import cn.dextea.trade.order.domain.model.Order;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository {
    Order save(Order order);

    Order getOrderById(Long orderId);

    Order getSummaryById(Long orderId);

    Order getSummaryByOrderNo(String orderNo);

    List<Order> getMonthOrders(Long customerId, LocalDateTime startAt, LocalDateTime endAt);

    List<Order> getStoreWindowOrders(Long storeId, LocalDateTime startAt, LocalDateTime endAt);

    void updatePaymentStatus(Order order);

    void updateMakingStatus(Order order);

    boolean timeoutOrder(Order order);
}
