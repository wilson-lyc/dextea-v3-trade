package cn.dextea.trade.order.application.assembler;

import cn.dextea.trade.order.application.dto.result.OrderDetailItem;
import cn.dextea.trade.order.application.dto.result.OrderDetailResult;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.model.OrderItem;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class OrderDetailAssembler {

    private OrderDetailAssembler() {
    }

    public static OrderDetailResult toResult(Order order) {
        if (order == null) {
            return null;
        }
        return OrderDetailResult.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .tradeNo(order.getTradeNo())
                .customerId(order.getCustomerId())
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
                .items(toItems(order.getItems()))
                .build();
    }

    private static List<OrderDetailItem> toItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        return items.stream().map(OrderDetailAssembler::toItem).collect(Collectors.toList());
    }

    private static OrderDetailItem toItem(OrderItem source) {
        return OrderDetailItem.builder()
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
