package cn.dextea.trade.order.interfaces.http.assembler;

import cn.dextea.trade.order.application.dto.command.GetStoreWindowOrdersCommand;
import cn.dextea.trade.order.application.dto.result.GetStoreWindowOrdersResult;
import cn.dextea.trade.order.interfaces.http.dto.request.GetStoreWindowOrdersRequest;
import cn.dextea.trade.order.interfaces.http.dto.response.GetStoreWindowOrdersResponse;
import cn.dextea.trade.order.interfaces.http.dto.response.StoreWindowOrderItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StoreOrderHttpAssembler {

    public GetStoreWindowOrdersCommand toCommand(GetStoreWindowOrdersRequest request, Long storeId) {
        return GetStoreWindowOrdersCommand.builder()
                .storeId(storeId)
                .hours(request.getHours())
                .build();
    }

    public GetStoreWindowOrdersResponse toResponse(GetStoreWindowOrdersResult result) {
        List<StoreWindowOrderItem> items = result.getItems().stream()
                .map(this::toItem)
                .toList();
        return GetStoreWindowOrdersResponse.builder()
                .items(items)
                .total(result.getTotal())
                .build();
    }

    private StoreWindowOrderItem toItem(GetStoreWindowOrdersResult.StoreWindowOrderItem source) {
        return StoreWindowOrderItem.builder()
                .orderId(source.getOrderId())
                .orderNo(source.getOrderNo())
                .totalPrice(source.getTotalPrice().getValue())
                .totalQuantity(source.getTotalQuantity().getValue())
                .diningMethod(source.getDiningMethod().getCode())
                .note(source.getNote())
                .makingStatus(source.getMakingStatus().getCode())
                .paymentMethod(source.getPaymentMethod().getCode())
                .paymentStatus(source.getPaymentStatus().getCode())
                .createdAt(source.getCreatedAt())
                .build();
    }
}
