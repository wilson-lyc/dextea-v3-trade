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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreBuildOrderUseCase {

    private final OrderCreationService orderCreationService;

    public PreBuildOrderResult execute(PreBuildOrderCommand command) {
        log.info("开始预构建订单, customerId={}, storeId={}, itemCount={}",
                command.getCustomerId(), command.getStoreId(), command.getItems().size());
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

        PreBuildOrderResult result = PreBuildOrderResult.builder()
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
