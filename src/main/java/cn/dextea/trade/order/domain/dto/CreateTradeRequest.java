package cn.dextea.trade.order.domain.dto;

import cn.dextea.trade.shared.domain.enumeration.PaymentMethod;
import cn.dextea.trade.shared.domain.money.Money;
import cn.dextea.trade.shared.domain.quantity.Quantity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTradeRequest {

    private String orderNo;

    private String buyerOpenId;

    private Money totalPrice;

    private Quantity totalQuantity;

    private PaymentMethod paymentMethod;

    private java.time.LocalDateTime payExpireAt;
}
