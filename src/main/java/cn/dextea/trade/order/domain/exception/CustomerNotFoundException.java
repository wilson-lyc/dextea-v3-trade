package cn.dextea.trade.order.domain.exception;

import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.shared.domain.error.BizError;

public class CustomerNotFoundException extends BizError {
    private final Long customerId;

    public CustomerNotFoundException(Long customerId) {
        super(OrderErrorCode.CUSTOMER_NOT_FOUND, "Customer not found: " + customerId);
        this.customerId = customerId;
    }

    public Long getCustomerId() {
        return customerId;
    }
}
