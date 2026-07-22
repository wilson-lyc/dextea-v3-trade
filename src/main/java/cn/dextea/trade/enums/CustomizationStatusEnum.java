package cn.dextea.trade.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 客制化项目全局状态枚举，对应 {@code customizations.status} 字段（tinyint）。
 */
@Getter
@RequiredArgsConstructor
public enum CustomizationStatusEnum {

    /** 禁用 */
    DISABLED(0, "禁用"),
    /** 激活 */
    ACTIVE(1, "激活");

    private final int code;
    private final String description;

    public static CustomizationStatusEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        for (CustomizationStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知客制化项目全局状态: " + code);
    }
}
