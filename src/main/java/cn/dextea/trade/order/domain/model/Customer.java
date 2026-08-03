package cn.dextea.trade.order.domain.model;

import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.enums.CustomerStatus;
import cn.dextea.trade.shared.domain.error.BizError;
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
    private CustomerStatus status;

    public boolean isActive() {
        return status == CustomerStatus.ACTIVE;
    }

    public void ensureActive() {
        if (!isActive()) {
            throw new BizError(OrderErrorCode.CUSTOMER_INACTIVE);
        }
    }
}
