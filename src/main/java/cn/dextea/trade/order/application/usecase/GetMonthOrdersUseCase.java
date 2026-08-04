package cn.dextea.trade.order.application.usecase;

import cn.dextea.trade.order.application.assembler.MonthOrderAssembler;
import cn.dextea.trade.order.application.dto.command.GetMonthOrdersCommand;
import cn.dextea.trade.order.application.dto.result.GetMonthOrdersResult;
import cn.dextea.trade.order.application.dto.result.MonthOrderItem;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.port.MonthOrderViewRepository;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.shared.domain.model.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GetMonthOrdersUseCase {

    private final OrderRepository orderRepository;
    private final MonthOrderViewRepository monthOrderViewRepository;

    public GetMonthOrdersResult execute(GetMonthOrdersCommand command) {
        List<Order> orders = orderRepository.getMonthOrders(
                command.getCustomerId(), command.getYear(), command.getMonth());

        Map<Long, String> storeNames = monthOrderViewRepository.findStoreNames(
                orders.stream().map(Order::getId).toList());

        List<MonthOrderItem> items = MonthOrderAssembler.toItems(orders, storeNames);

        Money totalAmount = Money.ZERO;
        for (MonthOrderItem order : items) {
            if (order.getTotalPrice() != null) {
                totalAmount = totalAmount.add(order.getTotalPrice());
            }
        }

        return GetMonthOrdersResult.builder()
                .orders(items)
                .orderCount(items.size())
                .totalAmount(totalAmount)
                .build();
    }
}
