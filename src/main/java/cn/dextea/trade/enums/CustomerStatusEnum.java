package cn.dextea.trade.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 顾客状态枚举，对应 {@code customers.status} 字段（tinyint）。
 */
@Getter
@RequiredArgsConstructor
public enum CustomerStatusEnum {

    /** 未激活 */
    INACTIVE(0, "未激活"),
    /** 激活 */
    ACTIVE(1, "激活");

    private final int code;
    private final String description;

    public static CustomerStatusEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        for (CustomerStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知顾客状态: " + code);
    }
}
