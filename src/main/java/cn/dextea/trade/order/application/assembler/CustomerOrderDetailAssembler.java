package cn.dextea.trade.order.application.assembler;

import cn.dextea.trade.order.application.dto.result.CustomerOrderDetailItem;
import cn.dextea.trade.order.application.dto.result.CustomerOrderDetailResult;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.model.OrderItem;

import static cn.dextea.trade.order.application.assembler.CustomerOrderItemAssembler.toOptionLabels;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class CustomerOrderDetailAssembler {

    private CustomerOrderDetailAssembler() {
    }

    public static CustomerOrderDetailResult toResult(Order order) {
        if (order == null) {
            return null;
        }
        return CustomerOrderDetailResult.builder()
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

    private static List<CustomerOrderDetailItem> toItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        return items.stream().map(CustomerOrderDetailAssembler::toItem).collect(Collectors.toList());
    }

    private static CustomerOrderDetailItem toItem(OrderItem source) {
        return CustomerOrderDetailItem.builder()
                .id(source.getId())
                .productId(source.getProductId())
                .productName(source.getProductName())
                .skuId(source.getSkuId())
                .customization(toOptionLabels(source.getCustomization()))
                .coverUrl(source.getCoverUrl())
                .quantity(source.getQuantity())
                .unitPrice(source.getUnitPrice())
                .totalPrice(source.getTotalPrice())
                .available(source.getAvailable())
                .build();
    }
}
