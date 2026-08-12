package cn.dextea.trade.order.application.assembler;

import cn.dextea.trade.order.application.dto.result.GetStoreWindowOrdersResult;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.shared.model.Quantity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StoreWindowOrderAssembler {

    public GetStoreWindowOrdersResult toResult(List<Order> orders) {
        List<GetStoreWindowOrdersResult.StoreWindowOrderItem> items = orders.stream()
                .map(this::toItem)
                .toList();
        return GetStoreWindowOrdersResult.builder()
                .items(items)
                .total((long) items.size())
                .build();
    }

    private GetStoreWindowOrdersResult.StoreWindowOrderItem toItem(Order order) {
        Quantity totalQuantity = order.getTotalQuantity();
        return GetStoreWindowOrdersResult.StoreWindowOrderItem.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .totalPrice(order.getTotalPrice())
                .totalQuantity(totalQuantity)
                .diningMethod(order.getDiningMethod())
                .note(order.getNote())
                .makingStatus(order.getMakingStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
