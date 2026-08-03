package cn.dextea.trade.order.application.assembler;

import cn.dextea.trade.order.application.dto.shared.CreateOrderItem;
import cn.dextea.trade.order.application.dto.shared.PreBuildOrderItem;
import cn.dextea.trade.order.domain.model.OrderItem;

import java.util.List;
import java.util.stream.Collectors;

public class OrderItemAssembler {

    public static CreateOrderItem toCreateItem(OrderItem orderItem) {
        return CreateOrderItem.builder()
                .skuId(orderItem.getSkuId())
                .quantity(orderItem.getQuantity())
                .product(orderItem.getProductName())
                .customization(toOptionLabels(orderItem.getCustomization()))
                .cover(orderItem.getCoverUrl())
                .unitPrice(orderItem.getUnitPrice())
                .totalPrice(orderItem.getTotalPrice())
                .available(orderItem.getAvailable())
                .build();
    }

    public static List<PreBuildOrderItem> toPreBuildItems(List<CreateOrderItem> items) {
        return items.stream()
                .map(item -> PreBuildOrderItem.builder()
                        .skuId(item.getSkuId())
                        .quantity(item.getQuantity())
                        .product(item.getProduct())
                        .customization(item.getCustomization())
                        .cover(item.getCover())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .available(item.getAvailable())
                        .build())
                .collect(Collectors.toList());
    }

    public static PreBuildOrderItem toPreBuildItem(OrderItem orderItem) {
        return PreBuildOrderItem.builder()
                .skuId(orderItem.getSkuId())
                .quantity(orderItem.getQuantity())
                .product(orderItem.getProductName())
                .customization(toOptionLabels(orderItem.getCustomization()))
                .cover(orderItem.getCoverUrl())
                .unitPrice(orderItem.getUnitPrice())
                .totalPrice(orderItem.getTotalPrice())
                .available(orderItem.getAvailable())
                .build();
    }

    public static String toOptionLabels(String customization) {
        if (customization == null || customization.isEmpty()) {
            return customization;
        }
        return java.util.Arrays.stream(customization.split("-"))
                .map(segment -> {
                    String[] parts = segment.split("_");
                    return parts[parts.length - 1];
                })
                .collect(Collectors.joining(" / "));
    }
}
