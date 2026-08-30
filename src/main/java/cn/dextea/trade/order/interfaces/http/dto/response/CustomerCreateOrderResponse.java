package cn.dextea.trade.order.interfaces.http.dto.response;
import cn.dextea.trade.order.interfaces.http.dto.shared.CustomerCreateOrderItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class CustomerCreateOrderResponse extends AbstractCustomerCreateOrderResponse<CustomerCreateOrderItem> {
    private Long id;

    private String orderNo;

    private String tradeNo;

    private LocalDateTime paymentExpiredAt;
}
