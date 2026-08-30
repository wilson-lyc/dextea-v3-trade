package cn.dextea.trade.order.interfaces.http.dto.request;
import cn.dextea.trade.order.interfaces.http.dto.shared.CustomerPreBuildOrderItem;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CustomerPreBuildOrderRequest extends AbstractCustomerCreateOrderRequest<CustomerPreBuildOrderItem> {
}
