package cn.dextea.trade.order.interfaces.http.dto.response;

import cn.dextea.trade.order.interfaces.http.dto.shared.AbstractOrderItem;
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
public abstract class AbstractCustomerCreateOrderResponse<T extends AbstractOrderItem> {
    private List<T> unavailable;

    private List<T> available;

    private Integer totalQuantity;

    private BigDecimal totalPrice;
}
