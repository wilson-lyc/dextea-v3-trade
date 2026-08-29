package cn.dextea.trade.order.interfaces.http.assembler;

import cn.dextea.trade.order.application.dto.command.CustomerGetOrderPaymentStatusCommand;
import cn.dextea.trade.order.application.dto.result.CustomerOrderPaymentStatusResult;
import cn.dextea.trade.order.domain.enumeration.PaymentStatus;
import cn.dextea.trade.order.interfaces.http.dto.response.CustomerOrderPaymentStatusResponse;

public final class CustomerOrderPaymentStatusHttpAssembler {

    private CustomerOrderPaymentStatusHttpAssembler() {
    }

    public static CustomerGetOrderPaymentStatusCommand toCommand(Long customerId, Long orderId) {
        return CustomerGetOrderPaymentStatusCommand.builder()
                .customerId(customerId)
                .orderId(orderId)
                .build();
    }

    public static CustomerOrderPaymentStatusResponse toResponse(CustomerOrderPaymentStatusResult result) {
        if (result == null) {
            return null;
        }
        return CustomerOrderPaymentStatusResponse.builder()
                .paymentStatus(toCode(result.getPaymentStatus()))
                .build();
    }

    private static Integer toCode(PaymentStatus codeEnum) {
        return codeEnum == null ? null : codeEnum.getCode();
    }
}
