package cn.dextea.trade.order.interfaces.assembler;

import cn.dextea.trade.order.application.command.CreateOrderCommand;
import cn.dextea.trade.order.application.command.OrderProductCommand;
import cn.dextea.trade.order.application.command.PreBuildOrderCommand;
import cn.dextea.trade.order.domain.model.OrderCreateResult;
import cn.dextea.trade.order.domain.model.OrderDetailView;
import cn.dextea.trade.order.domain.model.OrderSummaryView;
import cn.dextea.trade.order.domain.model.PricedOrderItem;
import cn.dextea.trade.order.domain.model.PreBuildResult;
import cn.dextea.trade.order.interfaces.dto.CreateOrderProductItem;
import cn.dextea.trade.order.interfaces.dto.CreateOrderRequest;
import cn.dextea.trade.order.interfaces.dto.CreateOrderResponse;
import cn.dextea.trade.order.interfaces.dto.CreateOrderUnavailable;
import cn.dextea.trade.order.interfaces.dto.CreateOrderUnavailableCustomization;
import cn.dextea.trade.order.interfaces.dto.CreateOrderUnavailableProduct;
import cn.dextea.trade.order.interfaces.dto.OrderDetailItem;
import cn.dextea.trade.order.interfaces.dto.OrderDetailResponse;
import cn.dextea.trade.order.interfaces.dto.OrderSummary;
import cn.dextea.trade.order.interfaces.dto.PreBuildOrderRequest;
import cn.dextea.trade.order.interfaces.dto.PreBuildOrderResponse;
import cn.dextea.trade.order.interfaces.dto.StoreInfo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单装配器：在请求 DTO / 领域值对象 / 响应 DTO 之间进行转换。
 */
public final class OrderAssembler {

    private OrderAssembler() {
    }

    public static PreBuildOrderCommand toPreBuildCommand(PreBuildOrderRequest request) {
        return PreBuildOrderCommand.builder()
                .storeId(request.getStoreId())
                .customerId(request.getCustomerId())
                .platform(request.getPlatform())
                .diningMethod(request.getDiningMethod())
                .note(request.getNote())
                .products(toProductCommands(request.getProducts()))
                .build();
    }

    public static CreateOrderCommand toCreateCommand(CreateOrderRequest request) {
        return CreateOrderCommand.builder()
                .storeId(request.getStoreId())
                .customerId(request.getCustomerId())
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
                .storeAvailable(result.isStoreAvailable())
                .customerAvailable(result.isCustomerAvailable())
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
                .unavailable(toUnavailable(pre))
                .products(toProductItems(pre.getProducts()))
                .storeAvailable(pre.isStoreAvailable())
                .customerAvailable(pre.isCustomerAvailable())
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

    public static OrderSummary toSummary(OrderSummaryView v) {
        return OrderSummary.builder()
                .storeName(v.getStoreName())
                .orderTime(v.getOrderTime())
                .tradeStatus(v.getTradeStatus())
                .tradeStatusDesc(v.getTradeStatusDesc())
                .makingStatus(v.getMakingStatus())
                .makingStatusDesc(v.getMakingStatusDesc())
                .totalPrice(v.getTotalPrice())
                .totalQuantity(v.getTotalQuantity())
                .coverUrls(v.getCoverUrls())
                .build();
    }

    public static OrderDetailResponse toDetail(OrderDetailView v) {
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
                .totalPrice(v.getTotalPrice())
                .totalQuantity(v.getTotalQuantity())
                .payMethod(v.getPayMethod())
                .payMethodDesc(v.getPayMethodDesc())
                .diningMethod(v.getDiningMethod())
                .diningMethodDesc(v.getDiningMethodDesc())
                .note(v.getNote())
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
