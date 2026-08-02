package cn.dextea.trade.order.application.dto.command;

import cn.dextea.trade.order.application.dto.shared.AbstractOrderItem;
import cn.dextea.trade.order.domain.model.enums.DiningMethod;
import cn.dextea.trade.order.domain.model.enums.OrderSource;
import cn.dextea.trade.order.domain.model.enums.PaymentMethod;
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
    private OrderSource source;
    private PaymentMethod paymentMethod;
    private DiningMethod diningMethod;
    private String note;
    private List<T> items;
}
