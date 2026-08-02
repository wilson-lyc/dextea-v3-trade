package cn.dextea.trade.order.interfaces.http.assembler;
import cn.dextea.trade.order.interfaces.http.dto.request.CreateOrderRequest;
import cn.dextea.trade.order.interfaces.http.dto.request.PreBuildOrderRequest;
import cn.dextea.trade.order.interfaces.http.dto.response.CreateOrderResponse;
import cn.dextea.trade.order.interfaces.http.dto.response.OrderDetailItem;
import cn.dextea.trade.order.interfaces.http.dto.response.OrderDetailResponse;
import cn.dextea.trade.order.interfaces.http.dto.response.OrderStatusResponse;
import cn.dextea.trade.order.interfaces.http.dto.response.OrderSummary;
import cn.dextea.trade.order.interfaces.http.dto.response.PreBuildOrderResponse;
import cn.dextea.trade.order.interfaces.http.dto.shared.CreateOrderItem;
import cn.dextea.trade.order.interfaces.http.dto.shared.PreBuildOrderItem;
import cn.dextea.trade.order.application.dto.command.CreateOrderCommand;
import cn.dextea.trade.order.application.dto.command.PreBuildOrderCommand;
import cn.dextea.trade.order.application.dto.result.OrderCreateResult;
import cn.dextea.trade.order.application.dto.OrderDetailDTO;
import cn.dextea.trade.order.application.dto.OrderStatusDTO;
import cn.dextea.trade.order.application.dto.OrderSummaryDTO;
import cn.dextea.trade.order.application.dto.result.PreBuildOrderResult;
import cn.dextea.trade.order.domain.enums.DiningMethodEnum;
import cn.dextea.trade.order.domain.model.enums.OrderSource;
import cn.dextea.trade.order.domain.model.enums.PaymentMethod;

import java.util.List;
public final class OrderHttpAssembler {
    private OrderHttpAssembler() {
    }
    public static PreBuildOrderCommand toPreBuildCommand(PreBuildOrderRequest request, Long customerId) {
        return PreBuildOrderCommand.builder()
                .storeId(request.getStoreId())
                .customerId(customerId)
                .diningMethod(DiningMethodEnum.of(request.getDiningMethod()))
                .source(OrderSource.of(request.getSource()))
                .paymentMethod(PaymentMethod.of(request.getPaymentMethod()))
                .note(request.getNote())
                .items(toPreBuildItems(request.getItems()))
                .build();
    }
    public static CreateOrderCommand toCreateCommand(CreateOrderRequest request, Long customerId) {
        return CreateOrderCommand.builder()
                .storeId(request.getStoreId())
                .customerId(customerId)
                .source(OrderSource.of(request.getSource()))
                .paymentMethod(PaymentMethod.of(request.getPaymentMethod()))
                .diningMethod(DiningMethodEnum.of(request.getDiningMethod()))
                .note(request.getNote())
                .items(toCreateItems(request.getItems()))
                .idempotencyKey(request.getIdempotencyKey())
                .build();
    }
    private static List<PreBuildOrderItem> toPreBuildItems(List<PreBuildOrderItem> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream()
                .map(i -> PreBuildOrderItem.builder().skuId(i.getSkuId()).quantity(i.getQuantity()).build())
                .toList();
    }
    private static List<CreateOrderItem> toCreateItems(List<CreateOrderItem> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream()
                .map(i -> CreateOrderItem.builder().skuId(i.getSkuId()).quantity(i.getQuantity()).build())
                .toList();
    }
    public static PreBuildOrderResponse toPreBuildResponse(PreBuildOrderResult result) {
        return PreBuildOrderResponse.builder()
                .unavailable(toResponseItems(result.getUnavailable()))
                .available(toResponseItems(result.getAvailable()))
                .totalQuantity(result.getTotalQuantity())
                .totalPrice(result.getTotalPrice())
                .build();
    }
    public static CreateOrderResponse toCreateResponse(OrderCreateResult result) {
        PreBuildOrderResult pre = result.getPreBuild();
        CreateOrderResponse.CreateOrderResponseBuilder builder = CreateOrderResponse.builder()
                .id(result.getId())
                .orderNo(result.getOrderNo())
                .tradeNo(result.getTradeNo())
                .paymentExpiredAt(result.getPaymentExpiredAt());
        if (pre != null) {
            builder.unavailable(toResponseItems(pre.getUnavailable()))
                    .available(toResponseItems(pre.getAvailable()))
                    .totalQuantity(pre.getTotalQuantity())
                    .totalPrice(pre.getTotalPrice());
        }
        return builder.build();
    }
    private static List<CreateOrderItem> toResponseItems(List<cn.dextea.trade.order.application.dto.CreateOrderItem> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream().map(i -> CreateOrderItem.builder()
                .skuId(i.getSkuId())
                .quantity(i.getQuantity())
                .productId(i.getProductId())
                .productName(i.getProductName())
                .cover(i.getCover())
                .customization(i.getCustomization())
                .unitPrice(i.getUnitPrice())
                .totalPrice(i.getTotalPrice())
                .build()).toList();
    }
    public static OrderSummary toSummary(OrderSummaryDTO v) {
        return OrderSummary.builder()
                .orderId(v.getOrderId())
                .storeName(v.getStoreName())
                .orderTime(v.getOrderTime())
                .tradeStatus(v.getTradeStatus())
                .makingStatus(v.getMakingStatus())
                .totalPrice(v.getTotalPrice())
                .totalQuantity(v.getTotalQuantity())
                .coverUrls(v.getCoverUrls())
                .build();
    }
    public static OrderStatusResponse toStatus(OrderStatusDTO v) {
        return OrderStatusResponse.builder()
                .orderId(v.getOrderId())
                .orderNo(v.getOrderNo())
                .tradeNo(v.getTradeNo())
                .tradeStatus(v.getTradeStatus())
                .makingStatus(v.getMakingStatus())
                .pickupCode(v.getPickupCode())
                .payExpireAt(v.getPayExpireAt())
                .paidAt(v.getPaidAt())
                .updatedAt(v.getUpdatedAt())
                .terminal(v.getTerminal())
                .build();
    }
    public static OrderDetailResponse toDetail(OrderDetailDTO v) {
        List<OrderDetailItem> items;
        if (v.getItems() == null) {
            items = List.of();
        } else {
            items = v.getItems().stream()
                    .<OrderDetailItem>map(i -> OrderDetailItem.builder()
                            .productId(i.getProductId())
                            .productName(i.getProductName())
                            .skuId(i.getSkuId())
                            .coverUrl(i.getCoverUrl())
                            .customizationText(i.getCustomizationText())
                            .quantity(i.getQuantity())
                            .unitPrice(i.getUnitPrice())
                            .subtotal(i.getSubtotal())
                            .build())
                    .toList();
        }
        return OrderDetailResponse.builder()
                .id(v.getId())
                .orderNo(v.getOrderNo())
                .tradeNo(v.getTradeNo())
                .tradeStatus(v.getTradeStatus())
                .makingStatus(v.getMakingStatus())
                .pickupCode(v.getPickupCode())
                .totalPrice(v.getTotalPrice())
                .totalQuantity(v.getTotalQuantity())
                .payMethod(v.getPayMethod())
                .diningMethod(v.getDiningMethod())
                .note(v.getNote())
                .payExpireAt(v.getPayExpireAt())
                .createdAt(v.getCreatedAt())
                .paidAt(v.getPaidAt())
                .refundedAt(v.getRefundedAt())
                .updatedAt(v.getUpdatedAt())
                .storeId(v.getStoreId())
                .storeName(v.getStoreName())
                .items(items)
                .build();
    }
}
