package cn.dextea.trade.order.application.assembler;

import cn.dextea.trade.order.application.dto.result.GetStoreMakingBoardResult;
import cn.dextea.trade.order.domain.enumeration.MakingStatus;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.shared.model.Quantity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StoreMakingBoardAssembler {

    public GetStoreMakingBoardResult toResult(List<Order> orders) {
        List<String> preparingPickupCodes = pickupCodesOf(orders, MakingStatus.PREPARING);
        List<String> readyPickupCodes = pickupCodesOf(orders, MakingStatus.READY);
        return GetStoreMakingBoardResult.builder()
                .preparingPickupCodes(preparingPickupCodes)
                .readyPickupCodes(readyPickupCodes)
                .preparingOrderCount((long) preparingPickupCodes.size())
                .preparingProductQuantity(quantitySumOf(orders, MakingStatus.PREPARING))
                .build();
    }

    private List<String> pickupCodesOf(List<Order> orders, MakingStatus makingStatus) {
        return orders.stream()
                .filter(order -> order.getMakingStatus() == makingStatus)
                .map(Order::getPickupCode)
                .toList();
    }

    private Quantity quantitySumOf(List<Order> orders, MakingStatus makingStatus) {
        return orders.stream()
                .filter(order -> order.getMakingStatus() == makingStatus)
                .map(Order::getTotalQuantity)
                .reduce(Quantity.ZERO, Quantity::add);
    }
}
