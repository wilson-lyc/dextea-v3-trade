package cn.dextea.trade.order.infrastructure.persistence.repository;

import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.model.OrderItem;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.order.infrastructure.persistence.converter.OrderConverter;
import cn.dextea.trade.order.infrastructure.persistence.mapper.OrderItemMapper;
import cn.dextea.trade.order.infrastructure.persistence.mapper.OrderMapper;
import cn.dextea.trade.order.infrastructure.persistence.po.OrderItemPO;
import cn.dextea.trade.order.infrastructure.persistence.po.OrderPO;
import cn.dextea.trade.shared.domain.error.BizError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderConverter orderConverter;

    @Override
    @Transactional
    public Order save(Order order) {
        OrderPO orderPO = orderConverter.toOrderPO(order);
        orderMapper.insert(orderPO);
        order.assignId(orderPO.getId());

        if (order.hasItems()) {
            List<OrderItemPO> itemPOs = new ArrayList<>();
            for (OrderItem item : order.getItems()) {
                item.assignOrderId(order.getId());
                itemPOs.add(orderConverter.toOrderItemPO(item));
            }
            orderItemMapper.batchInsert(itemPOs);
        }
        return order;
    }

    @Override
    public Order getById(Long orderId) {
        OrderPO orderPO = orderMapper.selectById(orderId);
        if (orderPO == null) {
            return null;
        }
        return orderConverter.toOrder(orderPO, getOrderItems(orderPO.getId()));
    }

    @Override
    public Order findByOrderNo(String orderNo) {
        OrderPO orderPO = orderMapper.selectByOrderNo(orderNo);
        if (orderPO == null) {
            return null;
        }
        return orderConverter.toOrder(orderPO, Collections.emptyList());
    }

    @Override
    @Transactional
    public void updatePaymentStatus(Order order) {
        OrderPO orderPO = orderConverter.toOrderPO(order);
        int updated = orderMapper.updatePaymentStatus(orderPO);
        if (updated == 0) {
            throw new BizError(OrderErrorCode.ORDER_UPDATE_CONFLICT);
        }
    }

    @Override
    public List<Order> getMonthOrders(Long customerId, int year, int month) {
        List<OrderPO> orderPOs = orderMapper.selectByCustomerAndMonth(customerId, year, month);
        if (orderPOs == null || orderPOs.isEmpty()) {
            return Collections.emptyList();
        }

        return orderPOs.stream()
                .map(po -> orderConverter.toOrder(po, getOrderItems(po.getId())))
                .collect(Collectors.toList());
    }

    private List<OrderItem> getOrderItems(Long orderId) {
        List<OrderItemPO> itemPOs = orderItemMapper.selectByOrderId(orderId);
        if (itemPOs == null || itemPOs.isEmpty()) {
            return Collections.emptyList();
        }
        return itemPOs.stream()
                .map(orderConverter::toOrderItem)
                .collect(Collectors.toList());
    }
}
