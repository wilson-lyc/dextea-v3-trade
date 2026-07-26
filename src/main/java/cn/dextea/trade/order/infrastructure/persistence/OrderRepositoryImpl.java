package cn.dextea.trade.order.infrastructure.persistence;

import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.model.OrderItem;
import cn.dextea.trade.order.domain.model.OrderStatusLog;
import cn.dextea.trade.order.domain.port.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单持久化端口实现：在 MyBatis Mapper 与领域模型之间做适配。
 *
 * <p>本实现直接以领域模型作为持久化对象（与 catalog 一致，避免过度设计），
 * 领域层通过 {@link OrderRepository} 端口隔离持久化细节。</p>
 */
@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order save(Order order) {
        orderMapper.insert(order);
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                item.setOrderId(order.getId());
            }
            orderItemMapper.batchInsert(order.getItems());
        }
        return order;
    }

    @Override
    public Order findByOrderNo(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }

    @Override
    public Order findById(Long id) {
        return orderMapper.selectById(id);
    }

    @Override
    public Order findByIdempotencyKey(String idempotencyKey) {
        return orderMapper.selectByIdempotencyKey(idempotencyKey);
    }

    @Override
    public void updateTradeNo(Long id, String tradeNo) {
        orderMapper.updateTradeNo(id, tradeNo);
    }

    @Override
    public int updateStatusCas(String orderNo, int targetStatus, int expectedStatus, int currentVersion,
                               String tradeNo, LocalDateTime paidAt, LocalDateTime refundedAt) {
        return orderMapper.updateStatusCas(orderNo, targetStatus, expectedStatus, currentVersion, tradeNo, paidAt, refundedAt);
    }

    @Override
    public void insertStatusLog(OrderStatusLog log) {
        orderStatusLogMapper.insert(log);
    }

    @Override
    public List<Order> findByCustomerIdAndCreatedAfter(Long customerId, LocalDateTime since) {
        return orderMapper.selectByCustomerIdAndCreatedAtAfter(customerId, since);
    }

    @Override
    public List<OrderItem> findItemsByOrderIds(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return List.of();
        }
        return orderItemMapper.selectByOrderIds(orderIds);
    }

    @Override
    public List<OrderItem> findFullItemsByOrderId(Long orderId) {
        return orderItemMapper.selectFullByOrderId(orderId);
    }
}
