package cn.dextea.trade.order.domain.port;

import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.model.OrderItem;
import cn.dextea.trade.order.domain.model.OrderStatusLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单持久化端口（由基础设施层实现）。
 */
public interface OrderRepository {

    Order save(Order order);

    Order findByOrderNo(String orderNo);

    Order findById(Long id);

    Order findByIdempotencyKey(String idempotencyKey);

    void updateTradeNo(Long id, String tradeNo);

    int updateStatusCas(String orderNo, int targetStatus, int expectedStatus, int currentVersion,
                        String tradeNo, LocalDateTime paidAt, LocalDateTime refundedAt);

    void insertStatusLog(OrderStatusLog log);

    List<Order> findByCustomerIdAndCreatedAfter(Long customerId, LocalDateTime since);

    List<OrderItem> findItemsByOrderIds(List<Long> orderIds);

    List<OrderItem> findFullItemsByOrderId(Long orderId);
}
