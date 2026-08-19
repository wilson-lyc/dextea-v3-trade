package cn.dextea.trade.order.application.assembler;

import cn.dextea.trade.order.application.dto.result.StoreOrderDetailItem;
import cn.dextea.trade.order.application.dto.result.StoreOrderDetailResult;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.model.OrderItem;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class StoreOrderDetailAssembler {

    private StoreOrderDetailAssembler() {
    }

    public static StoreOrderDetailResult toResult(Order order, List<OrderItem> items) {
        if (order == null) {
            return null;
        }
        return StoreOrderDetailResult.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .tradeNo(order.getTradeNo())
                .storeId(order.getStoreId())
                .diningMethod(order.getDiningMethod())
                .note(order.getNote())
                .source(order.getSource())
                .pickupCode(order.getPickupCode())
                .makingStatus(order.getMakingStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .paymentExpiredAt(order.getPaymentExpiredAt())
                .paymentPaidAt(order.getPaymentPaidAt())
                .paymentRefundedAt(order.getPaymentRefundedAt())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .totalPrice(order.getTotalPrice())
                .totalQuantity(order.getTotalQuantity())
                .items(toItems(items == null || items.isEmpty() ? order.getItems() : items))
                .build();
    }

    private static List<StoreOrderDetailItem> toItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        return items.stream().map(StoreOrderDetailAssembler::toItem).collect(Collectors.toList());
    }

    private static StoreOrderDetailItem toItem(OrderItem source) {
        return StoreOrderDetailItem.builder()
                .id(source.getId())
                .productId(source.getProductId())
                .productName(source.getProductName())
                .skuId(source.getSkuId())
                .customization(source.getCustomization())
                .coverUrl(source.getCoverUrl())
                .quantity(source.getQuantity())
                .unitPrice(source.getUnitPrice())
                .totalPrice(source.getTotalPrice())
                .available(source.getAvailable())
                .build();
    }
}
