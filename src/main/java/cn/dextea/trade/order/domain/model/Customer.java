package cn.dextea.trade.order.domain.model;

import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.enumeration.CustomerStatus;
import cn.dextea.trade.shared.error.BizError;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    private Long id;
    private String weixinOpenId;
    private String alipayOpenId;
    private CustomerStatus status;

    public void ensureActive() {
        if (status != CustomerStatus.ACTIVE) {
            throw new BizError(OrderErrorCode.CUSTOMER_INACTIVE);
        }
    }
}
