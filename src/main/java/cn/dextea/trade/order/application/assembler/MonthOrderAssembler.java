package cn.dextea.trade.order.application.assembler;

import cn.dextea.trade.order.application.dto.result.MonthOrderItem;
import cn.dextea.trade.order.domain.model.MonthOrderView;
import cn.dextea.trade.order.domain.model.Order;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class MonthOrderAssembler {

    private MonthOrderAssembler() {
    }

    public static List<MonthOrderItem> toItems(List<Order> orders, Map<Long, MonthOrderView> views) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }
        return orders.stream().map(order -> toItem(order, views.get(order.getId()))).collect(Collectors.toList());
    }

    public static MonthOrderItem toItem(Order source, MonthOrderView view) {
        if (source == null) {
            return null;
        }
        return MonthOrderItem.builder()
                .id(source.getId())
                .storeName(view == null ? null : view.storeName())
                .createdAt(source.getCreatedAt())
                .totalPrice(source.getTotalPrice())
                .totalQuantity(source.getTotalQuantity())
                .makingStatus(source.getMakingStatus())
                .paymentStatus(source.getPaymentStatus())
                .covers(view == null || view.covers() == null ? Collections.emptyList() : view.covers())
                .build();
    }
}

