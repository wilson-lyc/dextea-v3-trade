package cn.dextea.trade.order.application.dto.result;

import cn.dextea.trade.shared.model.Money;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class CustomerGetMonthOrdersResult {

    private List<CustomerMonthOrderItem> orders;

    private Integer orderCount;

    private Money totalAmount;
}
