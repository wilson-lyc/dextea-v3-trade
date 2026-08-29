package cn.dextea.trade.order.application.dto.command;

import cn.dextea.trade.order.application.dto.shared.AbstractCustomerOrderItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.util.List;

@Getter
@SuperBuilder
@NoArgsConstructor
public abstract class AbstractCustomerOrderCommand<T extends AbstractCustomerOrderItem> {
    private Long storeId;
    private Long customerId;
    private List<T> items;
}
