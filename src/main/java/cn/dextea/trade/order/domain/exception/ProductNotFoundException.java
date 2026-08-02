package cn.dextea.trade.order.domain.exception;

import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.shared.domain.error.BizError;

public class ProductNotFoundException extends BizError {
    private final Long productId;

    public ProductNotFoundException(Long productId) {
        super(OrderErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + productId);
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }
}
