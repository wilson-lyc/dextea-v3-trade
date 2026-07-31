package cn.dextea.trade.catalog.domain.exception;

import lombok.Getter;

@Getter
public enum CatalogErrorCode {
    PRODUCT_ID_INVALID(4001, "商品ID非法"),
    PRODUCT_UNAVAILABLE(4002, "商品不可用"),
    STORE_ID_INVALID(4003, "门店ID非法"),
    STORE_UNAVAILABLE(4004, "门店不可用"),
    CUSTOMER_ID_INVALID(4005, "顾客ID非法"),
    CUSTOMER_UNAVAILABLE(4006, "顾客不可用"),
    CUSTOMIZATION_ID_INVALID(4007, "客制化项目ID非法"),
    CUSTOMIZATION_OPTION_ID_INVALID(4008, "客制化选项ID非法"),
    CUSTOMIZATION_BINDING_INVALID(4009, "客制化绑定关系非法"),
    CUSTOMIZATION_UNAVAILABLE(4010, "客制化不可用");

    private final int code;
    private final String message;

    CatalogErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
