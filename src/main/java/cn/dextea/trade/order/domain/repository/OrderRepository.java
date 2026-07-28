package cn.dextea.trade.order.domain.repository;

import cn.dextea.trade.order.domain.model.entity.OrderStatusLog;
import cn.dextea.trade.order.domain.model.aggregate.Order;
import cn.dextea.trade.order.domain.model.entity.OrderItem;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单持久化仓储接口（由基础设施层实现）。
 */
public interface OrderRepository {

    Order save(Order order);

    Order findByOrderNo(String orderNo);

    Order findById(Long id);

    Order findByIdempotencyKey(String idempotencyKey);

    void updateTradeNo(Long id, String tradeNo);

    int updateStatusCas(String orderNo, int targetStatus, int expectedStatus, int currentVersion,
                        String tradeNo, LocalDateTime paidAt, LocalDateTime refundedAt, String pickupCode);

    void insertStatusLog(OrderStatusLog log);

    List<Order> findByCustomerIdAndCreatedBetween(Long customerId, LocalDateTime start, LocalDateTime end);

    List<OrderItem> findItemsByOrderIds(List<Long> orderIds);

    List<OrderItem> findFullItemsByOrderId(Long orderId);
}
