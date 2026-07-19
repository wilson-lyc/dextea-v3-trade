package cn.dextea.trade.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 门店状态枚举，对应 {@code product_store_status.status} 与
 * {@code customization_option_store_status.status} 字段（tinyint）。
 */
@Getter
@RequiredArgsConstructor
public enum ProductStoreStatus {

    /** 售罄 */
    SOLD_OUT(0, "售罄"),
    /** 可售 */
    AVAILABLE(1, "可售");

    private final int code;
    private final String description;

    public static ProductStoreStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProductStoreStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知门店状态: " + code);
    }
}
