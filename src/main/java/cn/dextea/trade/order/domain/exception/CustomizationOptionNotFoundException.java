package cn.dextea.trade.order.domain.exception;

import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.shared.domain.error.BizError;

public class CustomizationOptionNotFoundException extends BizError {
    private final Long optionId;

    public CustomizationOptionNotFoundException(Long optionId) {
        super(OrderErrorCode.CUSTOMIZATION_OPTION_NOT_FOUND, "Customization option not found: " + optionId);
        this.optionId = optionId;
    }

    public Long getOptionId() {
        return optionId;
    }
}
