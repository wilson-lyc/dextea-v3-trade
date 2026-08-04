package cn.dextea.trade.order.infrastructure.persistence.repository;

import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.model.OrderItem;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.order.infrastructure.adapter.RedisOrderItemCache;
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
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderConverter orderConverter;
    private final RedisOrderItemCache orderItemCache;

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
            orderItemCache.put(order.getId(), itemPOs);
        }
        return order;
    }

    @Override
    public Order getOrderById(Long orderId) {
        OrderPO orderPO = orderMapper.selectById(orderId);
        if (orderPO == null) {
            return null;
        }
        return orderConverter.toOrder(orderPO, getOrderItems(orderPO.getId()));
    }

    @Override
    public Order getSummaryById(Long orderId) {
        OrderPO orderPO = orderMapper.selectById(orderId);
        if (orderPO == null) {
            return null;
        }
        return orderConverter.toOrder(orderPO, Collections.emptyList());
    }

    @Override
    public Order getSummaryByOrderNo(String orderNo) {
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

        Map<Long, List<OrderItem>> itemsByOrderId = getOrderItemsByOrderIds(
                orderPOs.stream().map(OrderPO::getId).collect(Collectors.toList()));
        return orderPOs.stream()
                .map(po -> orderConverter.toOrder(po,
                        itemsByOrderId.getOrDefault(po.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    private Map<Long, List<OrderItem>> getOrderItemsByOrderIds(List<Long> orderIds) {
        Map<Long, List<OrderItemPO>> itemsByOrderId = orderItemCache.getMulti(orderIds);
        List<Long> missingOrderIds = orderIds.stream()
                .filter(id -> !itemsByOrderId.containsKey(id))
                .toList();
        if (!missingOrderIds.isEmpty()) {
            List<OrderItemPO> dbItems = orderItemMapper.selectByOrderIds(missingOrderIds);
            if (dbItems != null && !dbItems.isEmpty()) {
                Map<Long, List<OrderItemPO>> dbMap = dbItems.stream()
                        .collect(Collectors.groupingBy(OrderItemPO::getOrderId));
                dbMap.forEach(orderItemCache::put);
                itemsByOrderId.putAll(dbMap);
            }
        }
        return itemsByOrderId.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> e.getValue().stream().map(orderConverter::toOrderItem).collect(Collectors.toList())));
    }

    private List<OrderItem> getOrderItems(Long orderId) {
        List<OrderItemPO> cached = orderItemCache.get(orderId);
        if (cached != null) {
            return cached.stream().map(orderConverter::toOrderItem).collect(Collectors.toList());
        }
        List<OrderItemPO> itemPOs = orderItemMapper.selectByOrderId(orderId);
        if (itemPOs == null || itemPOs.isEmpty()) {
            return Collections.emptyList();
        }
        orderItemCache.put(orderId, itemPOs);
        return itemPOs.stream().map(orderConverter::toOrderItem).collect(Collectors.toList());
    }
}
