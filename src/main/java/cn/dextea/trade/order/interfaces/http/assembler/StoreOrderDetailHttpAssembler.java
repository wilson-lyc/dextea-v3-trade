package cn.dextea.trade.order.interfaces.http.assembler;

import cn.dextea.trade.order.application.dto.command.GetStoreOrderDetailCommand;
import cn.dextea.trade.order.application.dto.result.StoreOrderDetailItem;
import cn.dextea.trade.order.application.dto.result.StoreOrderDetailResult;
import cn.dextea.trade.order.domain.enumeration.DiningMethod;
import cn.dextea.trade.order.domain.enumeration.MakingStatus;
import cn.dextea.trade.order.domain.enumeration.OrderSource;
import cn.dextea.trade.shared.enumeration.PaymentMethod;
import cn.dextea.trade.order.domain.enumeration.PaymentStatus;
import cn.dextea.trade.order.interfaces.http.dto.response.StoreOrderDetailItemResponse;
import cn.dextea.trade.order.interfaces.http.dto.response.StoreOrderDetailResponse;
import cn.dextea.trade.shared.model.Money;
import cn.dextea.trade.shared.model.Quantity;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public final class StoreOrderDetailHttpAssembler {

    private StoreOrderDetailHttpAssembler() {
    }

    public static GetStoreOrderDetailCommand toCommand(Long storeId, Long orderId) {
        return GetStoreOrderDetailCommand.builder()
                .storeId(storeId)
                .orderId(orderId)
                .build();
    }

    public static StoreOrderDetailResponse toResponse(StoreOrderDetailResult result) {
        if (result == null) {
            return null;
        }
        return StoreOrderDetailResponse.builder()
                .id(result.getId())
                .orderNo(result.getOrderNo())
                .tradeNo(result.getTradeNo())
                .storeId(result.getStoreId())
                .diningMethod(toCode(result.getDiningMethod()))
                .note(result.getNote())
                .source(toCode(result.getSource()))
                .pickupCode(result.getPickupCode())
                .makingStatus(toCode(result.getMakingStatus()))
                .paymentMethod(toCode(result.getPaymentMethod()))
                .paymentStatus(toCode(result.getPaymentStatus()))
                .paymentExpiredAt(result.getPaymentExpiredAt())
                .paymentPaidAt(result.getPaymentPaidAt())
                .paymentRefundedAt(result.getPaymentRefundedAt())
                .createdAt(result.getCreatedAt())
                .updatedAt(result.getUpdatedAt())
                .totalPrice(toDecimal(result.getTotalPrice()))
                .totalQuantity(toInt(result.getTotalQuantity()))
                .items(toResponseItems(result.getItems()))
                .build();
    }

    private static List<StoreOrderDetailItemResponse> toResponseItems(List<StoreOrderDetailItem> sources) {
        if (sources == null) {
            return null;
        }
        return sources.stream().map(StoreOrderDetailHttpAssembler::toResponseItem).collect(Collectors.toList());
    }

    private static StoreOrderDetailItemResponse toResponseItem(StoreOrderDetailItem source) {
        return StoreOrderDetailItemResponse.builder()
                .id(source.getId())
                .productId(source.getProductId())
                .productName(source.getProductName())
                .skuId(source.getSkuId())
                .customization(source.getCustomization())
                .coverUrl(source.getCoverUrl())
                .quantity(toInt(source.getQuantity()))
                .unitPrice(toDecimal(source.getUnitPrice()))
                .totalPrice(toDecimal(source.getTotalPrice()))
                .available(source.getAvailable())
                .build();
    }

    private static Integer toCode(DiningMethod codeEnum) {
        return codeEnum == null ? null : codeEnum.getCode();
    }

    private static Integer toCode(OrderSource codeEnum) {
        return codeEnum == null ? null : codeEnum.getCode();
    }

    private static Integer toCode(MakingStatus codeEnum) {
        return codeEnum == null ? null : codeEnum.getCode();
    }

    private static Integer toCode(PaymentMethod codeEnum) {
        return codeEnum == null ? null : codeEnum.getCode();
    }

    private static Integer toCode(PaymentStatus codeEnum) {
        return codeEnum == null ? null : codeEnum.getCode();
    }

    private static BigDecimal toDecimal(Money money) {
        return money == null ? null : money.getValue();
    }

    private static Integer toInt(Quantity quantity) {
        return quantity == null ? null : quantity.getValue();
    }
}
