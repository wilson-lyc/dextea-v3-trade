package cn.dextea.trade.order.interfaces.http.assembler;

import cn.dextea.trade.order.application.dto.command.CreateOrderCommand;
import cn.dextea.trade.order.application.dto.command.PreBuildOrderCommand;
import cn.dextea.trade.order.application.dto.result.OrderCreateResult;
import cn.dextea.trade.order.application.dto.result.PreBuildOrderResult;
import cn.dextea.trade.order.application.dto.shared.CreateOrderItem;
import cn.dextea.trade.order.domain.model.enums.DiningMethod;
import cn.dextea.trade.order.interfaces.http.dto.request.CreateOrderRequest;
import cn.dextea.trade.order.interfaces.http.dto.request.PreBuildOrderRequest;
import cn.dextea.trade.order.interfaces.http.dto.response.CreateOrderResponse;
import cn.dextea.trade.order.interfaces.http.dto.response.PreBuildOrderResponse;

import java.util.List;
import java.util.stream.Collectors;

public final class OrderHttpAssembler {

    private OrderHttpAssembler() {
    }

    public static PreBuildOrderCommand toPreBuildCommand(PreBuildOrderRequest request, Long customerId) {
        return PreBuildOrderCommand.builder()
                .storeId(request.getStoreId())
                .customerId(customerId)
                .source(request.getSource())
                .paymentMethod(request.getPaymentMethod())
                .diningMethod(toDiningMethod(request.getDiningMethod()))
                .note(request.getNote())
                .items(toAppItems(request.getItems(), cn.dextea.trade.order.application.dto.shared.PreBuildOrderItem::new))
                .build();
    }

    public static CreateOrderCommand toCreateCommand(CreateOrderRequest request, Long customerId) {
        return CreateOrderCommand.builder()
                .storeId(request.getStoreId())
                .customerId(customerId)
                .source(request.getSource())
                .paymentMethod(request.getPaymentMethod())
                .diningMethod(toDiningMethod(request.getDiningMethod()))
                .note(request.getNote())
                .idempotencyKey(request.getIdempotencyKey())
                .items(toAppItems(request.getItems(), CreateOrderItem::new))
                .build();
    }

    public static PreBuildOrderResponse toPreBuildResponse(PreBuildOrderResult result) {
        return PreBuildOrderResponse.builder()
                .unavailable(toWebItems(result.getUnavailable(), cn.dextea.trade.order.interfaces.http.dto.shared.PreBuildOrderItem::new))
                .available(toWebItems(result.getAvailable(), cn.dextea.trade.order.interfaces.http.dto.shared.PreBuildOrderItem::new))
                .totalQuantity(result.getTotalQuantity())
                .totalPrice(result.getTotalPrice())
                .build();
    }

    public static CreateOrderResponse toCreateResponse(OrderCreateResult result) {
        PreBuildOrderResult preBuild = result.getPreBuild();
        CreateOrderResponse.CreateOrderResponseBuilder<?, ?> builder = CreateOrderResponse.builder()
                .id(result.getId())
                .orderNo(result.getOrderNo())
                .tradeNo(result.getTradeNo())
                .paymentExpiredAt(result.getPaymentExpiredAt());
        if (preBuild != null) {
            builder.unavailable(toWebItems(preBuild.getUnavailable(), cn.dextea.trade.order.interfaces.http.dto.shared.CreateOrderItem::new))
                    .available(toWebItems(preBuild.getAvailable(), cn.dextea.trade.order.interfaces.http.dto.shared.CreateOrderItem::new))
                    .totalQuantity(preBuild.getTotalQuantity())
                    .totalPrice(preBuild.getTotalPrice());
        }
        return builder.build();
    }

    private static DiningMethod toDiningMethod(Integer code) {
        if (code == null) {
            return null;
        }
        for (DiningMethod value : DiningMethod.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("非法的 diningMethod: " + code);
    }

    private static List<cn.dextea.trade.order.application.dto.shared.AbstractOrderItem> toAppItems(
            List<cn.dextea.trade.order.interfaces.http.dto.shared.AbstractOrderItem> sources,
            java.util.function.Supplier<cn.dextea.trade.order.application.dto.shared.AbstractOrderItem> factory) {
        if (sources == null) {
            return null;
        }
        return sources.stream().map(source -> {
            cn.dextea.trade.order.application.dto.shared.AbstractOrderItem item = factory.get();
            copyToApp(source, item);
            return item;
        }).collect(Collectors.toList());
    }

    private static List<cn.dextea.trade.order.interfaces.http.dto.shared.AbstractOrderItem> toWebItems(
            List<cn.dextea.trade.order.application.dto.shared.AbstractOrderItem> sources,
            java.util.function.Supplier<cn.dextea.trade.order.interfaces.http.dto.shared.AbstractOrderItem> factory) {
        if (sources == null) {
            return null;
        }
        return sources.stream().map(source -> {
            cn.dextea.trade.order.interfaces.http.dto.shared.AbstractOrderItem item = factory.get();
            copyToWeb(source, item);
            return item;
        }).collect(Collectors.toList());
    }

    private static void copyToApp(
            cn.dextea.trade.order.interfaces.http.dto.shared.AbstractOrderItem source,
            cn.dextea.trade.order.application.dto.shared.AbstractOrderItem target) {
        target.setSkuId(source.getSkuId());
        target.setQuantity(source.getQuantity());
        target.setProductId(source.getProductId());
        target.setProductName(source.getProductName());
        target.setCover(source.getCover());
        target.setCustomization(source.getCustomization());
        target.setUnitPrice(source.getUnitPrice());
        target.setTotalPrice(source.getTotalPrice());
    }

    private static void copyToWeb(
            cn.dextea.trade.order.application.dto.shared.AbstractOrderItem source,
            cn.dextea.trade.order.interfaces.http.dto.shared.AbstractOrderItem target) {
        target.setSkuId(source.getSkuId());
        target.setQuantity(source.getQuantity());
        target.setProductId(source.getProductId());
        target.setProductName(source.getProductName());
        target.setCover(source.getCover());
        target.setCustomization(source.getCustomization());
        target.setUnitPrice(source.getUnitPrice());
        target.setTotalPrice(source.getTotalPrice());
    }
}
