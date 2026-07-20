package cn.dextea.trade.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 客制化选项全局状态枚举，对应 {@code customization_options.status} 字段（tinyint）。
 */
@Getter
@RequiredArgsConstructor
public enum CustomizationOptionGlobalStatus {

    /** 禁用 */
    DISABLED(0, "禁用"),
    /** 激活 */
    ACTIVE(1, "激活");

    private final int code;
    private final String description;

    public static CustomizationOptionGlobalStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (CustomizationOptionGlobalStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知客制化选项全局状态: " + code);
    }
}
