package cn.dextea.trade.order.interfaces.http.dto.response;
import cn.dextea.trade.order.interfaces.http.dto.shared.CustomerPreBuildOrderItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class CustomerPreBuildOrderResponse extends AbstractCustomerCreateOrderResponse<CustomerPreBuildOrderItem> {
}
