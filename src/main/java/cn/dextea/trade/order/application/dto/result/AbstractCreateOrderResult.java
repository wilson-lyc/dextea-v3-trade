package cn.dextea.trade.order.application.dto.result;

import cn.dextea.trade.order.application.dto.shared.AbstractOrderItem;
import cn.dextea.trade.shared.domain.money.Money;
import cn.dextea.trade.shared.domain.quantity.Quantity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class AbstractCreateOrderResult<T extends AbstractOrderItem> {
    private List<T> unavailable;

    private List<T> available;

    private Quantity totalQuantity;

    private Money totalPrice;
}
