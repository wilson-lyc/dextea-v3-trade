package cn.dextea.trade.order.domain.exception;

import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.shared.domain.error.BizError;

public class StoreNotFoundException extends BizError {
    private final Long storeId;

    public StoreNotFoundException(Long storeId) {
        super(OrderErrorCode.STORE_NOT_FOUND, "Store not found: " + storeId);
        this.storeId = storeId;
    }

    public Long getStoreId() {
        return storeId;
    }
}
