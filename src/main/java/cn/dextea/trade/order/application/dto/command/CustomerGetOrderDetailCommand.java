package cn.dextea.trade.order.application.dto.command;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class CustomerGetOrderDetailCommand {

    private Long customerId;

    private Long orderId;
}
