package cn.dextea.trade.order.interfaces.http.assembler;

import cn.dextea.trade.order.application.dto.command.CustomerGetMonthOrdersCommand;
import cn.dextea.trade.order.application.dto.result.CustomerGetMonthOrdersResult;
import cn.dextea.trade.order.application.dto.result.CustomerMonthOrderItem;
import cn.dextea.trade.order.interfaces.http.dto.request.CustomerGetMonthOrdersRequest;
import cn.dextea.trade.order.interfaces.http.dto.response.CustomerGetMonthOrdersResponse;
import cn.dextea.trade.shared.model.Money;
import cn.dextea.trade.shared.model.Quantity;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

public final class CustomerMonthOrderHttpAssembler {

    private CustomerMonthOrderHttpAssembler() {
    }

    public static CustomerGetMonthOrdersCommand toCommand(CustomerGetMonthOrdersRequest request, Long customerId) {
        YearMonth ym = YearMonth.of(request.getYear(), request.getMonth());
        return CustomerGetMonthOrdersCommand.builder()
                .customerId(customerId)
                .year(request.getYear())
                .month(request.getMonth())
                .startAt(ym.atDay(1).atStartOfDay())
                .endAt(ym.plusMonths(1).atDay(1).atStartOfDay())
                .build();
    }

    public static CustomerGetMonthOrdersResponse toResponse(CustomerGetMonthOrdersResult result) {
        if (result == null) {
            return null;
        }
        return CustomerGetMonthOrdersResponse.builder()
                .orders(toResponseItems(result.getOrders()))
                .totalCount(result.getOrderCount())
                .totalAmount(toDecimal(result.getTotalAmount()))
                .build();
    }

    private static List<cn.dextea.trade.order.interfaces.http.dto.response.CustomerMonthOrderItem> toResponseItems(
            List<CustomerMonthOrderItem> sources) {
        if (sources == null) {
            return null;
        }
        return sources.stream().map(CustomerMonthOrderHttpAssembler::toResponseItem).collect(Collectors.toList());
    }

    private static cn.dextea.trade.order.interfaces.http.dto.response.CustomerMonthOrderItem toResponseItem(CustomerMonthOrderItem source) {
        return cn.dextea.trade.order.interfaces.http.dto.response.CustomerMonthOrderItem.builder()
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
