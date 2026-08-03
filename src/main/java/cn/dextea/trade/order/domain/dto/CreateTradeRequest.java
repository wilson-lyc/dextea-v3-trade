package cn.dextea.trade.order.domain.dto;

import cn.dextea.trade.shared.domain.enumeration.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTradeRequest {

    private String orderNo;

    private String buyerOpenId;

    private BigDecimal totalPrice;

    private Integer totalQuantity;

    private PaymentMethod paymentMethod;

    private java.time.LocalDateTime payExpireAt;
}
