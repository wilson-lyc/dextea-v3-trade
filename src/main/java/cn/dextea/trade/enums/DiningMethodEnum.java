package cn.dextea.trade.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 用餐方式枚举，对应 {@code orders.dining_method} 字段（tinyint）。
 *
 * <p>数据库与下单请求均使用数值，{@code 0} 为堂食，{@code 1} 为外带。</p>
 */
@Getter
@RequiredArgsConstructor
public enum DiningMethodEnum {

    /** 堂食 */
    DINE_IN(0, "堂食"),
    /** 外带 */
    TAKEOUT(1, "外带");

    private final int code;
    private final String description;

    /**
     * 按数值解析用餐方式
     *
     * @param code 数值（0 堂食 / 1 外带）
     * @return 用餐方式（空则 null）
     */
    public static DiningMethodEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        for (DiningMethodEnum method : values()) {
            if (method.code == code) {
                return method;
            }
        }
        throw new IllegalArgumentException("未知用餐方式: " + code);
    }
}
