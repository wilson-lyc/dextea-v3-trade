package cn.dextea.trade.order.application.usecase;

import cn.dextea.trade.order.application.assembler.CustomerOrderItemAssembler;
import cn.dextea.trade.order.application.dto.command.CustomerPreBuildOrderCommand;
import cn.dextea.trade.order.application.dto.result.CustomerPreBuildOrderResult;
import cn.dextea.trade.order.application.dto.shared.CustomerPreBuildOrderItem;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.model.OrderItem;
import cn.dextea.trade.order.domain.model.SkuItem;
import cn.dextea.trade.order.domain.service.OrderCreationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerPreBuildOrderUseCase {

    private final OrderCreationService orderCreationService;

    @Transactional(readOnly = true)
    public CustomerPreBuildOrderResult execute(CustomerPreBuildOrderCommand command) {
        log.info("开始预构建订单, customerId={}, storeId={}, itemCount={}",
                command.getCustomerId(), command.getStoreId(), command.getItems().size());
        List<SkuItem> skuItems = CustomerOrderItemAssembler.toSkuItems(command.getItems());

        Order order = orderCreationService.preBuildOrder(command.getCustomerId(), command.getStoreId(), skuItems);

        List<CustomerPreBuildOrderItem> availableItems = new ArrayList<>();
        List<CustomerPreBuildOrderItem> unavailableItems = new ArrayList<>();
        for (OrderItem orderItem : order.getItems()) {
            CustomerPreBuildOrderItem item = CustomerOrderItemAssembler.toPreBuildItem(orderItem);
            if (orderItem.getAvailable()) {
                availableItems.add(item);
            } else {
                unavailableItems.add(item);
            }
        }

        CustomerPreBuildOrderResult result = CustomerPreBuildOrderResult.builder()
                .available(availableItems)
                .unavailable(unavailableItems)
                .totalQuantity(order.getTotalQuantity())
                .totalPrice(order.getTotalPrice())
                .build();
        log.info("预构建订单完成, customerId={}, storeId={}, availableCount={}, unavailableCount={}, totalPrice={}",
                command.getCustomerId(), command.getStoreId(),
                availableItems.size(), unavailableItems.size(), order.getTotalPrice());
        return result;
    }
}
