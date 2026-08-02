package cn.dextea.trade.order.application.dto.result;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class AbstractCreateOrderResult {
    private List<CreateOrderItem> unavailable;

    private List<CreateOrderItem> available;

    private Integer totalQuantity;

    private BigDecimal totalPrice;
}
