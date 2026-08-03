package cn.dextea.trade.order.infrastructure.persistence.repository;

import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.model.OrderItem;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.order.infrastructure.persistence.converter.OrderConverter;
import cn.dextea.trade.order.infrastructure.persistence.mapper.OrderItemMapper;
import cn.dextea.trade.order.infrastructure.persistence.mapper.OrderMapper;
import cn.dextea.trade.order.infrastructure.persistence.po.OrderItemPO;
import cn.dextea.trade.order.infrastructure.persistence.po.OrderPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
}
