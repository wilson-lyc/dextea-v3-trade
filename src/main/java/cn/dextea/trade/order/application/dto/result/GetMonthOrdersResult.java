package cn.dextea.trade.order.application.dto.result;

import cn.dextea.trade.shared.domain.model.Money;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class GetMonthOrdersResult {

    private List<MonthOrderItem> orders;

    private Integer orderCount;

    private Money totalAmount;
}
