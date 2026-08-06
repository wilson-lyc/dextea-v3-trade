package cn.dextea.trade.order.interfaces.http.assembler;

import cn.dextea.trade.order.application.dto.command.GetMonthOrdersCommand;
import cn.dextea.trade.order.application.dto.result.GetMonthOrdersResult;
import cn.dextea.trade.order.application.dto.result.MonthOrderItem;
import cn.dextea.trade.order.interfaces.http.dto.request.GetMonthOrdersRequest;
import cn.dextea.trade.order.interfaces.http.dto.response.GetMonthOrdersResponse;
import cn.dextea.trade.shared.model.Money;
import cn.dextea.trade.shared.model.Quantity;

import java.util.List;
import java.util.stream.Collectors;

public final class MonthOrderHttpAssembler {

    private MonthOrderHttpAssembler() {
    }

    public static GetMonthOrdersCommand toCommand(GetMonthOrdersRequest request, Long customerId) {
        return GetMonthOrdersCommand.builder()
                .customerId(customerId)
                .year(request.getYear())
                .month(request.getMonth())
                .build();
    }

    public static GetMonthOrdersResponse toResponse(GetMonthOrdersResult result) {
        if (result == null) {
            return null;
        }
        return GetMonthOrdersResponse.builder()
                .orders(toResponseItems(result.getOrders()))
                .totalCount(result.getOrderCount())
                .totalAmount(toDecimal(result.getTotalAmount()))
                .build();
    }

    private static List<cn.dextea.trade.order.interfaces.http.dto.response.MonthOrderItem> toResponseItems(
            List<MonthOrderItem> sources) {
        if (sources == null) {
            return null;
        }
        return sources.stream().map(MonthOrderHttpAssembler::toResponseItem).collect(Collectors.toList());
    }

    private static cn.dextea.trade.order.interfaces.http.dto.response.MonthOrderItem toResponseItem(MonthOrderItem source) {
        return cn.dextea.trade.order.interfaces.http.dto.response.MonthOrderItem.builder()
                .id(source.getId())
                .storeName(source.getStoreName())
                .createdAt(source.getCreatedAt())
                .totalPrice(toDecimal(source.getTotalPrice()))
                .totalQuantity(toInt(source.getTotalQuantity()))
                .makingStatus(source.getMakingStatus() == null ? null : source.getMakingStatus().getCode())
                .paymentStatus(source.getPaymentStatus() == null ? null : source.getPaymentStatus().getCode())
                .covers(source.getCovers())
                .build();
    }

    private static java.math.BigDecimal toDecimal(Money money) {
        return money == null ? null : money.getValue();
    }

    private static Integer toInt(Quantity quantity) {
        return quantity == null ? null : quantity.getValue();
    }
}
