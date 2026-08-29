package cn.dextea.trade.order.application.assembler;

import cn.dextea.trade.order.application.dto.shared.AbstractCustomerOrderItem;
import cn.dextea.trade.order.application.dto.shared.CustomerCreateOrderItem;
import cn.dextea.trade.order.application.dto.shared.CustomerPreBuildOrderItem;
import cn.dextea.trade.order.domain.model.OrderItem;
import cn.dextea.trade.order.domain.model.SkuItem;

import java.util.List;
import java.util.stream.Collectors;

public class CustomerOrderItemAssembler {

    public static List<SkuItem> toSkuItems(List<? extends AbstractCustomerOrderItem> items) {
        return items.stream()
                .map(item -> new SkuItem(item.getSkuId(), item.getQuantity()))
                .collect(Collectors.toList());
    }

    public static CustomerCreateOrderItem toCreateItem(OrderItem orderItem) {
        return CustomerCreateOrderItem.builder()
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

    public static List<CustomerPreBuildOrderItem> toPreBuildItems(List<CustomerCreateOrderItem> items) {
        return items.stream()
                .map(item -> CustomerPreBuildOrderItem.builder()
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

    public static CustomerPreBuildOrderItem toPreBuildItem(OrderItem orderItem) {
        return CustomerPreBuildOrderItem.builder()
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
