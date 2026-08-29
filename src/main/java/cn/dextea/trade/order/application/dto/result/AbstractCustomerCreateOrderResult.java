package cn.dextea.trade.order.application.dto.result;

import cn.dextea.trade.order.application.dto.shared.AbstractCustomerOrderItem;
import cn.dextea.trade.shared.model.Money;
import cn.dextea.trade.shared.model.Quantity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class AbstractCustomerCreateOrderResult<T extends AbstractCustomerOrderItem> {
    private List<T> unavailable;

    private List<T> available;

    private Quantity totalQuantity;

    private Money totalPrice;
}
