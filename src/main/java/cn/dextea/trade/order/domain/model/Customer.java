package cn.dextea.trade.order.domain.model;

import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.enumeration.CustomerStatus;
import cn.dextea.trade.shared.domain.error.Activatable;
import cn.dextea.trade.shared.domain.error.BizErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer implements Activatable {
    private Long id;
    private String weixinOpenId;
    private String alipayOpenId;
    private CustomerStatus status;

    @Override
    public boolean isActive() {
        return status == CustomerStatus.ACTIVE;
    }

    @Override
    public BizErrorCode inactiveErrorCode() {
        return OrderErrorCode.CUSTOMER_INACTIVE;
    }
}
