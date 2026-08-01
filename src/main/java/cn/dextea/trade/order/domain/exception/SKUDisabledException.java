package cn.dextea.trade.order.domain.exception;

import lombok.Getter;

@Getter
public class SKUDisabledException extends RuntimeException {
    private final Long productId;
    private final String productName;
    private final String skuId;
    private final String customization;

    public SKUDisabledException(String message, Long productId, String productName, String skuId, String customization) {
        super(message);
        this.productId = productId;
        this.productName = productName;
        this.skuId = skuId;
        this.customization = customization;
    }
}
