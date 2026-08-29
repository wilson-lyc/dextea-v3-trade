package cn.dextea.trade.order.interfaces.http.dto.response;
import cn.dextea.trade.order.interfaces.http.dto.shared.CustomerPreBuildOrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "预构建订单响应")
public class CustomerPreBuildOrderResponse extends AbstractCustomerCreateOrderResponse<CustomerPreBuildOrderItem> {
}
