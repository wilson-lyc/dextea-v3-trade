package cn.dextea.trade.order.domain.exception;

import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.shared.domain.error.BizError;

public class CustomizationItemNotFoundException extends BizError {
    private final Long itemId;

    public CustomizationItemNotFoundException(Long itemId) {
        super(OrderErrorCode.CUSTOMIZATION_ITEM_NOT_FOUND, "Customization item not found: " + itemId);
        this.itemId = itemId;
    }

    public Long getItemId() {
        return itemId;
    }
}
