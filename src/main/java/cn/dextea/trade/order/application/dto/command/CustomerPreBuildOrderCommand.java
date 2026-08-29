package cn.dextea.trade.order.application.dto.command;
import cn.dextea.trade.order.application.dto.shared.CustomerPreBuildOrderItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class CustomerPreBuildOrderCommand extends AbstractCustomerOrderCommand<CustomerPreBuildOrderItem> {
}
