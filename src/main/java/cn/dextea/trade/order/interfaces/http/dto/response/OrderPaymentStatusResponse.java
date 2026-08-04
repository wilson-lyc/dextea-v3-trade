package cn.dextea.trade.order.interfaces.http.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "订单支付状态响应")
public class OrderPaymentStatusResponse {

    @Schema(description = "支付状态", example = "2")
    private Integer paymentStatus;
}
