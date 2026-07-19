package cn.dextea.trade.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 商品全局状态枚举，对应 {@code products.status} 字段（tinyint）。
 */
@Getter
@RequiredArgsConstructor
public enum ProductGlobalStatus {

    /** 下架 */
    OFF_SHELF(0, "下架"),
    /** 上架 */
    ON_SHELF(1, "上架");

    private final int code;
    private final String description;

    public static ProductGlobalStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProductGlobalStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知商品全局状态: " + code);
    }
}
