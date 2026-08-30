package cn.dextea.trade.order.interfaces.http.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class CustomerOrderPaymentStatusResponse {

    private Integer paymentStatus;
}
