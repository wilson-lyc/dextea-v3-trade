package cn.dextea.trade.order.interfaces.http.assembler;

import cn.dextea.trade.order.application.dto.command.GetOrderPaymentStatusCommand;
import cn.dextea.trade.order.application.dto.result.OrderPaymentStatusResult;
import cn.dextea.trade.order.domain.model.enumeration.PaymentStatus;
import cn.dextea.trade.order.interfaces.http.dto.response.OrderPaymentStatusResponse;

public final class OrderPaymentStatusHttpAssembler {

    private OrderPaymentStatusHttpAssembler() {
    }

    public static GetOrderPaymentStatusCommand toCommand(Long customerId, Long orderId) {
        return GetOrderPaymentStatusCommand.builder()
                .customerId(customerId)
                .orderId(orderId)
                .build();
    }

    public static OrderPaymentStatusResponse toResponse(OrderPaymentStatusResult result) {
        if (result == null) {
            return null;
        }
        return OrderPaymentStatusResponse.builder()
                .paymentStatus(toCode(result.getPaymentStatus()))
                .build();
    }

    private static Integer toCode(PaymentStatus codeEnum) {
        return codeEnum == null ? null : codeEnum.getCode();
    }
}
