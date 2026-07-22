package cn.dextea.trade.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 门店业务状态枚举，对应 {@code stores.status} 字段（tinyint）。
 */
@Getter
@RequiredArgsConstructor
public enum StoreStatusEnum {

    /** 停业 */
    CLOSED(0, "停业"),
    /** 营业中 */
    OPEN(1, "营业中");

    private final int code;
    private final String description;

    public static StoreStatusEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        for (StoreStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知门店状态: " + code);
    }
}
