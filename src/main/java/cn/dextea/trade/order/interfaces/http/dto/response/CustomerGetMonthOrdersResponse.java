package cn.dextea.trade.order.interfaces.http.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class CustomerGetMonthOrdersResponse {

    private List<CustomerMonthOrderItem> orders;

    private Integer totalCount;

    private BigDecimal totalAmount;
}
