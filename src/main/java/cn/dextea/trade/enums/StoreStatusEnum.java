package cn.dextea.trade.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StoreStatusEnum {

    CLOSED(0, "停业"),
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
