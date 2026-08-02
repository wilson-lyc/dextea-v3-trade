package cn.dextea.trade.order.application.dto.command;
import cn.dextea.trade.pay.domain.enums.PlatformEnum;
import cn.dextea.trade.order.application.dto.shared.CreateOrderItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class CreateOrderCommand extends AbstractOrderCommand<CreateOrderItem> {
    private String idempotencyKey;
}
