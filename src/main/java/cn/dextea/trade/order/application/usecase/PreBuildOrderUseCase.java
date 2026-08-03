package cn.dextea.trade.order.application.usecase;

import cn.dextea.trade.order.application.assembler.OrderItemAssembler;
import cn.dextea.trade.order.application.dto.command.PreBuildOrderCommand;
import cn.dextea.trade.order.application.dto.result.PreBuildOrderResult;
import cn.dextea.trade.order.application.dto.shared.PreBuildOrderItem;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.model.OrderItem;
import cn.dextea.trade.order.domain.model.SkuItem;
import cn.dextea.trade.order.domain.service.OrderCreationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PreBuildOrderUseCase {

    private final OrderCreationService orderCreationService;

    public PreBuildOrderResult execute(PreBuildOrderCommand command) {
        List<SkuItem> skuItems = OrderItemAssembler.toSkuItems(command.getItems());

        Order order = orderCreationService.preBuildOrder(command.getCustomerId(), command.getStoreId(), skuItems);

        List<PreBuildOrderItem> availableItems = new ArrayList<>();
        List<PreBuildOrderItem> unavailableItems = new ArrayList<>();
        for (OrderItem orderItem : order.getItems()) {
            PreBuildOrderItem item = OrderItemAssembler.toPreBuildItem(orderItem);
            if (orderItem.getAvailable()) {
                availableItems.add(item);
            } else {
                unavailableItems.add(item);
            }
        }

        return PreBuildOrderResult.builder()
                .available(availableItems)
                .unavailable(unavailableItems)
                .totalQuantity(order.getTotalQuantity())
                .totalPrice(order.getTotalPrice())
                .build();
    }
}
