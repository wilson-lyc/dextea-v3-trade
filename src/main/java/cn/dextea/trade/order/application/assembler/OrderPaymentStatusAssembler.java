package cn.dextea.trade.order.application.assembler;

import cn.dextea.trade.order.application.dto.result.OrderPaymentStatusResult;
import cn.dextea.trade.order.domain.model.Order;

public final class OrderPaymentStatusAssembler {

    private OrderPaymentStatusAssembler() {
    }

    public static OrderPaymentStatusResult toResult(Order order) {
        if (order == null) {
            return null;
        }
        return OrderPaymentStatusResult.builder()
                .paymentStatus(order.getPaymentStatus())
                .build();
    }
}
