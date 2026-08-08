package cn.dextea.trade.order.application.usecase;

import cn.dextea.trade.order.application.assembler.MonthOrderAssembler;
import cn.dextea.trade.order.application.dto.command.GetMonthOrdersCommand;
import cn.dextea.trade.order.application.dto.result.GetMonthOrdersResult;
import cn.dextea.trade.order.application.dto.result.MonthOrderItem;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.model.Store;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.order.domain.repository.StoreRepository;
import cn.dextea.trade.shared.model.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetMonthOrdersUseCase {

    private static final String UNKNOWN_STORE_NAME = "未知门店";

    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;

    public GetMonthOrdersResult execute(GetMonthOrdersCommand command) {
        log.info("查询月订单, customerId={}, year={}, month={}",
                command.getCustomerId(), command.getYear(), command.getMonth());
        List<Order> orders = orderRepository.getMonthOrders(
                command.getCustomerId(), command.getStartAt(), command.getEndAt());

        Set<Long> storeIds = orders.stream()
                .map(Order::getStoreId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Store> storeById = storeRepository.getStoresByIds(storeIds);
        Map<Long, String> storeNames = orders.stream().collect(Collectors.toMap(
                Order::getId,
                order -> resolveStoreName(storeById.get(order.getStoreId()))));

        List<MonthOrderItem> items = MonthOrderAssembler.toItems(orders, storeNames);

        Money totalAmount = Money.ZERO;
        for (MonthOrderItem order : items) {
            if (order.getTotalPrice() != null) {
                totalAmount = totalAmount.add(order.getTotalPrice());
            }
        }

        GetMonthOrdersResult result = GetMonthOrdersResult.builder()
                .orders(items)
                .orderCount(items.size())
                .totalAmount(totalAmount)
                .build();
        log.info("查询月订单完成, customerId={}, year={}, month={}, orderCount={}, totalAmount={}",
                command.getCustomerId(), command.getYear(), command.getMonth(),
                result.getOrderCount(), result.getTotalAmount());
        return result;
    }

    private String resolveStoreName(Store store) {
        return store == null || store.getName() == null ? UNKNOWN_STORE_NAME : store.getName();
    }
}
