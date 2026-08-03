package cn.dextea.trade.order.interfaces.http.assembler;

import cn.dextea.trade.order.application.dto.command.CreateOrderCommand;
import cn.dextea.trade.order.application.dto.command.PreBuildOrderCommand;
import cn.dextea.trade.order.application.dto.result.OrderCreateResult;
import cn.dextea.trade.order.application.dto.result.PreBuildOrderResult;
import cn.dextea.trade.order.application.dto.shared.CreateOrderItem;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.enumeration.DiningMethod;
import cn.dextea.trade.order.domain.model.enumeration.OrderSource;
import cn.dextea.trade.shared.domain.enumeration.PaymentMethod;
import cn.dextea.trade.order.interfaces.http.dto.request.CreateOrderRequest;
import cn.dextea.trade.order.interfaces.http.dto.request.PreBuildOrderRequest;
import cn.dextea.trade.order.interfaces.http.dto.response.CreateOrderResponse;
import cn.dextea.trade.order.interfaces.http.dto.response.PreBuildOrderResponse;
import cn.dextea.trade.shared.domain.enumeration.EnumUtils;
import cn.dextea.trade.shared.domain.model.Quantity;

import java.util.List;
import java.util.stream.Collectors;

public final class OrderHttpAssembler {

    private OrderHttpAssembler() {
    }

    public static PreBuildOrderCommand toPreBuildCommand(PreBuildOrderRequest request, Long customerId) {
        return PreBuildOrderCommand.builder()
                .storeId(request.getStoreId())
                .customerId(customerId)
                .items(toAppItems(request.getItems(), cn.dextea.trade.order.application.dto.shared.PreBuildOrderItem::new))
                .build();
    }

    public static CreateOrderCommand toCreateCommand(CreateOrderRequest request, Long customerId) {
        return CreateOrderCommand.builder()
                .storeId(request.getStoreId())
                .customerId(customerId)
                .source(EnumUtils.toEnum(OrderSource::of, request.getSource(), OrderErrorCode.INVALID_ORDER_SOURCE))
                .paymentMethod(EnumUtils.toEnum(PaymentMethod::of, request.getPaymentMethod(), OrderErrorCode.INVALID_PAYMENT_METHOD))
                .diningMethod(EnumUtils.toEnum(DiningMethod::of, request.getDiningMethod(), OrderErrorCode.INVALID_DINING_METHOD))
                .note(request.getNote())
                .idempotencyKey(request.getIdempotencyKey())
                .items(toAppItems(request.getItems(), CreateOrderItem::new))
                .build();
    }

    public static PreBuildOrderResponse toPreBuildResponse(PreBuildOrderResult result) {
        return PreBuildOrderResponse.builder()
                .unavailable(toWebItems(result.getUnavailable(), cn.dextea.trade.order.interfaces.http.dto.shared.PreBuildOrderItem::new))
                .available(toWebItems(result.getAvailable(), cn.dextea.trade.order.interfaces.http.dto.shared.PreBuildOrderItem::new))
                .totalQuantity(result.getTotalQuantity() == null ? null : result.getTotalQuantity().getValue())
                .totalPrice(result.getTotalPrice() == null ? null : result.getTotalPrice().getValue())
                .build();
    }

    public static CreateOrderResponse toCreateResponse(OrderCreateResult result) {
        return CreateOrderResponse.builder()
                .id(result.getId())
                .orderNo(result.getOrderNo())
                .tradeNo(result.getTradeNo())
                .paymentExpiredAt(result.getPaymentExpiredAt())
                .unavailable(toWebItems(result.getUnavailable(), cn.dextea.trade.order.interfaces.http.dto.shared.CreateOrderItem::new))
                .available(toWebItems(result.getAvailable(), cn.dextea.trade.order.interfaces.http.dto.shared.CreateOrderItem::new))
                .totalQuantity(result.getTotalQuantity() == null ? null : result.getTotalQuantity().getValue())
                .totalPrice(result.getTotalPrice() == null ? null : result.getTotalPrice().getValue())
                .build();
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
        target.setQuantity(Quantity.of(source.getQuantity() == null ? 0 : source.getQuantity()));
    }

    private static void copyToWeb(
            cn.dextea.trade.order.application.dto.shared.AbstractOrderItem source,
            cn.dextea.trade.order.interfaces.http.dto.shared.AbstractOrderItem target) {
        target.setSkuId(source.getSkuId());
        target.setQuantity(source.getQuantity() == null ? 0 : source.getQuantity().getValue());
        target.setProduct(source.getProduct());
        target.setCustomization(source.getCustomization());
        target.setCover(source.getCover());
        target.setUnitPrice(source.getUnitPrice() == null ? null : source.getUnitPrice().getValue());
        target.setTotalPrice(source.getTotalPrice() == null ? null : source.getTotalPrice().getValue());
    }
}
