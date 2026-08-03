package cn.dextea.trade.order.application.dto.command;
import cn.dextea.trade.order.application.dto.shared.CreateOrderItem;
import cn.dextea.trade.order.domain.model.enums.DiningMethod;
import cn.dextea.trade.order.domain.model.enums.OrderSource;
import cn.dextea.trade.shared.domain.enumeration.PaymentMethod;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class CreateOrderCommand extends AbstractOrderCommand<CreateOrderItem> {
    private String idempotencyKey;
    private OrderSource source;
    private PaymentMethod paymentMethod;
    private DiningMethod diningMethod;
    private String note;
}
