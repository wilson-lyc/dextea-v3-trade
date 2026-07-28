package cn.dextea.trade.order.api.assembler;

import cn.dextea.trade.order.api.dto.request.CreateOrderProductItem;
import cn.dextea.trade.order.api.dto.request.CreateOrderRequest;
import cn.dextea.trade.order.api.dto.request.PreBuildOrderRequest;
import cn.dextea.trade.order.api.dto.response.CreateOrderResponse;
import cn.dextea.trade.order.api.dto.response.CreateOrderUnavailable;
import cn.dextea.trade.order.api.dto.response.CreateOrderUnavailableCustomization;
import cn.dextea.trade.order.api.dto.response.CreateOrderUnavailableProduct;
import cn.dextea.trade.order.api.dto.response.OrderDetailItem;
import cn.dextea.trade.order.api.dto.response.OrderDetailResponse;
import cn.dextea.trade.order.api.dto.response.OrderStatusResponse;
import cn.dextea.trade.order.api.dto.response.OrderSummary;
import cn.dextea.trade.order.api.dto.response.PreBuildOrderResponse;
import cn.dextea.trade.order.api.dto.response.StoreInfo;
import cn.dextea.trade.order.application.command.CreateOrderCommand;
import cn.dextea.trade.order.application.command.OrderProductCommand;
import cn.dextea.trade.order.application.command.PreBuildOrderCommand;
import cn.dextea.trade.order.application.dto.OrderCreateResult;
import cn.dextea.trade.order.application.dto.OrderDetailDTO;
import cn.dextea.trade.order.application.dto.OrderStatusDTO;
import cn.dextea.trade.order.application.dto.OrderSummaryDTO;
import cn.dextea.trade.order.domain.model.valueobject.PreBuildResult;
import cn.dextea.trade.order.domain.model.valueobject.PricedOrderItem;

import java.util.List;

/**
 * 订单接口层装配器：在请求 DTO / 应用层命令与 DTO / 响应 DTO 之间进行转换。
 */
public final class OrderApiAssembler {

    private OrderApiAssembler() {
    }

    public static PreBuildOrderCommand toPreBuildCommand(PreBuildOrderRequest request, Long customerId) {
        return PreBuildOrderCommand.builder()
                .storeId(request.getStoreId())
                .customerId(customerId)
                .platform(request.getPlatform())
                .diningMethod(request.getDiningMethod())
                .note(request.getNote())
                .products(toProductCommands(request.getProducts()))
                .build();
    }

    public static CreateOrderCommand toCreateCommand(CreateOrderRequest request, Long customerId) {
        return CreateOrderCommand.builder()
                .storeId(request.getStoreId())
                .customerId(customerId)
                .platform(request.getPlatform())
                .diningMethod(request.getDiningMethod())
                .note(request.getNote())
                .products(toProductCommands(request.getProducts()))
                .idempotencyKey(request.getIdempotencyKey())
                .build();
    }

    private static List<OrderProductCommand> toProductCommands(List<CreateOrderProductItem> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream()
                .map(i -> OrderProductCommand.builder().skuId(i.getSkuId()).quantity(i.getQuantity()).build())
                .toList();
    }

    public static PreBuildOrderResponse toPreBuildResponse(PreBuildResult result) {
        return PreBuildOrderResponse.builder()
                .unavailable(toUnavailable(result))
                .products(toProductItems(result.getProducts()))
                .totalQuantity(result.getTotalQuantity())
                .totalPrice(result.getTotalPrice())
                .build();
    }

    public static CreateOrderResponse toCreateResponse(OrderCreateResult result) {
        PreBuildResult pre = result.getPreBuild();
        return CreateOrderResponse.builder()
                .id(result.getId())
                .orderNo(result.getOrderNo())
                .tradeNo(result.getTradeNo())
                .payExpireAt(result.getPayExpireAt())
                .unavailable(toUnavailable(pre))
                .products(toProductItems(pre.getProducts()))
                .totalQuantity(pre.getTotalQuantity())
                .totalPrice(pre.getTotalPrice())
                .build();
    }

    private static CreateOrderUnavailable toUnavailable(PreBuildResult result) {
        return CreateOrderUnavailable.builder()
                .products(result.getUnavailableProducts() == null ? null
                        : result.getUnavailableProducts().stream()
                        .map(p -> CreateOrderUnavailableProduct.builder().id(p.getId()).name(p.getName()).build())
                        .toList())
                .customization(result.getUnavailableCustomizations() == null ? null
                        : result.getUnavailableCustomizations().stream()
                        .map(c -> CreateOrderUnavailableCustomization.builder()
                                .optionId(c.getOptionId()).optionName(c.getOptionName())
                                .productId(c.getProductId()).productName(c.getProductName())
                                .itemId(c.getItemId()).itemName(c.getItemName()).build())
                        .toList())
                .build();
    }

    private static List<CreateOrderProductItem> toProductItems(List<PricedOrderItem> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream().map(p -> CreateOrderProductItem.builder()
                .skuId(p.getSkuId())
                .quantity(p.getQuantity())
                .productId(p.getProductId())
                .productName(p.getProductName())
                .coverId(p.getCoverId())
                .coverUrl(p.getCoverUrl())
                .customizationText(p.getCustomizationText())
                .unitPrice(p.getUnitPrice())
                .subtotal(p.getSubtotal())
                .build()).toList();
    }

    public static OrderSummary toSummary(OrderSummaryDTO v) {
        return OrderSummary.builder()
                .storeName(v.getStoreName())
                .orderTime(v.getOrderTime())
                .tradeStatus(v.getTradeStatus())
                .tradeStatusDesc(v.getTradeStatusDesc())
                .makingStatus(v.getMakingStatus())
                .makingStatusDesc(v.getMakingStatusDesc())
                .totalPrice(v.getTotalPrice())
                .totalQuantity(v.getTotalQuantity())
                .payExpireAt(v.getPayExpireAt())
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
                .tradeStatusDesc(v.getTradeStatusDesc())
                .makingStatus(v.getMakingStatus())
                .makingStatusDesc(v.getMakingStatusDesc())
                .pickupCode(v.getPickupCode())
                .totalPrice(v.getTotalPrice())
                .totalQuantity(v.getTotalQuantity())
                .payMethod(v.getPayMethod())
                .payMethodDesc(v.getPayMethodDesc())
                .diningMethod(v.getDiningMethod())
                .diningMethodDesc(v.getDiningMethodDesc())
                .note(v.getNote())
                .payExpireAt(v.getPayExpireAt())
                .createdAt(v.getCreatedAt())
                .paidAt(v.getPaidAt())
                .refundedAt(v.getRefundedAt())
                .updatedAt(v.getUpdatedAt())
                .store(v.getStore() == null ? null : StoreInfo.builder()
                        .id(v.getStore().getId())
                        .name(v.getStore().getName())
                        .address(v.getStore().getAddress())
                        .phone(v.getStore().getPhone())
                        .businessHours(v.getStore().getBusinessHours())
                        .build())
                .items(items)
                .build();
    }
}
