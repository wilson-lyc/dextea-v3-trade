package cn.dextea.trade.order.application.assembler;

import cn.dextea.trade.order.application.dto.result.CustomerOrderPaymentStatusResult;
import cn.dextea.trade.order.domain.model.Order;

public final class CustomerOrderPaymentStatusAssembler {

    private CustomerOrderPaymentStatusAssembler() {
    }

    public static CustomerOrderPaymentStatusResult toResult(Order order) {
        if (order == null) {
            return null;
        }
        return CustomerOrderPaymentStatusResult.builder()
                .paymentStatus(order.getPaymentStatus())
                .build();
    }
}
