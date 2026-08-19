package cn.dextea.trade.order.application.usecase;

import cn.dextea.trade.order.application.dto.command.GetOrderDetailCommand;
import cn.dextea.trade.order.application.dto.result.OrderDetailResult;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.model.OrderItem;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetOrderDetailUseCase {

    private final OrderRepository orderRepository;

    public OrderDetailResult execute(GetOrderDetailCommand command) {
        Order order = orderRepository.getById(command.getOrderId());
        List<OrderItem> items = orderRepository.findOrderItems(command.getOrderId());
        return OrderDetailResult.builder()
                .order(order)
                .items(items)
                .build();
    }
}
