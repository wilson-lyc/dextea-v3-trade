package cn.dextea.trade.order.application.command;

import cn.dextea.trade.order.domain.model.enums.DiningMethod;
import cn.dextea.trade.order.domain.model.enums.OrderSource;
import cn.dextea.trade.order.domain.model.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreBuildOrderCommand {
    private Long storeId;
    private Long customerId;
    private DiningMethod diningMethod;
    private String note;
    private OrderSource source;
    private PaymentMethod paymentMethod;
    private List<OrderProductCommand> products;
}
