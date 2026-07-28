package cn.dextea.trade.order.infrastructure.persistence.repository;

import cn.dextea.trade.order.domain.enums.MakingStatusEnum;
import cn.dextea.trade.order.domain.enums.TradeStatusEnum;
import cn.dextea.trade.order.domain.model.entity.OrderStatusLog;
import cn.dextea.trade.order.domain.model.aggregate.Order;
import cn.dextea.trade.order.domain.model.entity.OrderItem;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.order.infrastructure.persistence.mapper.OrderItemMapper;
import cn.dextea.trade.order.infrastructure.persistence.po.OrderPO;
import cn.dextea.trade.order.infrastructure.persistence.mapper.OrderMapper;
import cn.dextea.trade.order.infrastructure.persistence.mapper.OrderStatusLogMapper;
import cn.dextea.trade.order.infrastructure.persistence.translator.OrderTranslator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单仓储实现：在 MyBatis Mapper（PO）与领域模型之间做适配。
 *
 * <p>持久化对象（PO）与领域模型在此通过 {@link OrderTranslator} 互转，
 * 领域层只接触 {@link Order} / {@link OrderItem} / {@link OrderStatusLog}，
 * 完全不感知库表结构与 MyBatis 细节。</p>
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
        if (order.getTradeStatus() == null) {
            order.setTradeStatus(TradeStatusEnum.TRADE_WAIT_PAY.getCode());
        }
        if (order.getMakingStatus() == null) {
            order.setMakingStatus(MakingStatusEnum.MAKING_WAIT.getCode());
        }
        OrderPO orderPO = OrderTranslator.toOrderPO(order);
        orderMapper.insert(orderPO);
        order.setId(orderPO.getId());
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                item.setOrderId(order.getId());
            }
            orderItemMapper.batchInsert(OrderTranslator.toOrderItemPOs(order.getItems()));
        }
        return order;
    }

    @Override
    public Order findByOrderNo(String orderNo) {
        return OrderTranslator.toOrder(orderMapper.selectByOrderNo(orderNo));
    }

    @Override
    public Order findById(Long id) {
        return OrderTranslator.toOrder(orderMapper.selectById(id));
    }

    @Override
    public Order findByIdempotencyKey(String idempotencyKey) {
        return OrderTranslator.toOrder(orderMapper.selectByIdempotencyKey(idempotencyKey));
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
        orderStatusLogMapper.insert(OrderTranslator.toOrderStatusLogPO(log));
    }

    @Override
    public List<Order> findByCustomerIdAndCreatedBetween(Long customerId, LocalDateTime start, LocalDateTime end) {
        return OrderTranslator.toOrders(orderMapper.selectByCustomerIdAndCreatedBetween(customerId, start, end));
    }

    @Override
    public List<OrderItem> findItemsByOrderIds(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return List.of();
        }
        return OrderTranslator.toOrderItems(orderItemMapper.selectByOrderIds(orderIds));
    }

    @Override
    public List<OrderItem> findFullItemsByOrderId(Long orderId) {
        return OrderTranslator.toOrderItems(orderItemMapper.selectFullByOrderId(orderId));
    }
}
