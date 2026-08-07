package cn.dextea.trade.order.application.dto.result;

import cn.dextea.trade.order.domain.enumeration.MakingStatus;
import cn.dextea.trade.order.domain.enumeration.PaymentStatus;
import cn.dextea.trade.shared.model.Money;
import cn.dextea.trade.shared.model.Quantity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class MonthOrderItem {

    private Long id;

    private String storeName;

    private LocalDateTime createdAt;

    private Money totalPrice;

    private Quantity totalQuantity;

    private MakingStatus makingStatus;

    private PaymentStatus paymentStatus;

    private List<String> covers;
}
