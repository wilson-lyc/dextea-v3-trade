package cn.dextea.trade.order.application.assembler;

import cn.dextea.trade.order.application.dto.result.MonthOrderItem;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.model.OrderItem;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class MonthOrderAssembler {

    private MonthOrderAssembler() {
    }

    public static List<MonthOrderItem> toItems(List<Order> orders, Map<Long, String> storeNames) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }
        return orders.stream().map(order -> toItem(order, storeNames.get(order.getId()))).collect(Collectors.toList());
    }

    public static MonthOrderItem toItem(Order source, String storeName) {
        if (source == null) {
            return null;
        }
        return MonthOrderItem.builder()
                .id(source.getId())
                .storeName(storeName)
                .createdAt(source.getCreatedAt())
                .totalPrice(source.getTotalPrice())
                .totalQuantity(source.getTotalQuantity())
                .makingStatus(source.getMakingStatus())
                .paymentStatus(source.getPaymentStatus())
                .covers(source.getItems().stream()
                        .map(OrderItem::getCoverUrl)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .build();
    }
}

