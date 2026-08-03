package cn.dextea.trade.order.application.dto.command;

import cn.dextea.trade.order.application.dto.shared.AbstractOrderItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.util.List;

@Getter
@SuperBuilder
@NoArgsConstructor
public abstract class AbstractOrderCommand<T extends AbstractOrderItem> {
    private Long storeId;
    private Long customerId;
    private List<T> items;
}
