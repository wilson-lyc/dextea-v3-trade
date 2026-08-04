package cn.dextea.trade.order.application.dto.result;

import cn.dextea.trade.order.domain.model.enumeration.PaymentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class OrderPaymentStatusResult {

    private PaymentStatus paymentStatus;
}
