package cn.dextea.trade.order.interfaces.http.assembler;

import cn.dextea.trade.order.application.dto.command.CustomerGetOrderDetailCommand;
import cn.dextea.trade.order.application.dto.result.CustomerOrderDetailResult;
import cn.dextea.trade.order.domain.enumeration.DiningMethod;
import cn.dextea.trade.order.domain.enumeration.MakingStatus;
import cn.dextea.trade.order.domain.enumeration.OrderSource;
import cn.dextea.trade.shared.enumeration.PaymentMethod;
import cn.dextea.trade.order.domain.enumeration.PaymentStatus;
import cn.dextea.trade.order.interfaces.http.dto.response.CustomerOrderDetailItem;
import cn.dextea.trade.order.interfaces.http.dto.response.CustomerOrderDetailResponse;
import cn.dextea.trade.shared.model.Money;
import cn.dextea.trade.shared.model.Quantity;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public final class CustomerOrderDetailHttpAssembler {

    private CustomerOrderDetailHttpAssembler() {
    }

    public static CustomerGetOrderDetailCommand toCommand(Long customerId, Long orderId) {
        return CustomerGetOrderDetailCommand.builder()
                .customerId(customerId)
                .orderId(orderId)
                .build();
    }

    public static CustomerOrderDetailResponse toResponse(CustomerOrderDetailResult result) {
        if (result == null) {
            return null;
        }
        return CustomerOrderDetailResponse.builder()
                .id(result.getId())
                .orderNo(result.getOrderNo())
                .tradeNo(result.getTradeNo())
                .customerId(result.getCustomerId())
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

    private static List<CustomerOrderDetailItem> toResponseItems(
            List<cn.dextea.trade.order.application.dto.result.CustomerOrderDetailItem> sources) {
        if (sources == null) {
            return null;
        }
        return sources.stream().map(CustomerOrderDetailHttpAssembler::toResponseItem).collect(Collectors.toList());
    }

    private static CustomerOrderDetailItem toResponseItem(
            cn.dextea.trade.order.application.dto.result.CustomerOrderDetailItem source) {
        return CustomerOrderDetailItem.builder()
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
