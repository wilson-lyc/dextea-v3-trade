package cn.dextea.trade.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StoreStatusEnum implements CodeEnum {

    CLOSED(0, "停业"),
    OPEN(1, "营业中");

    private final int code;
    private final String description;

    public static StoreStatusEnum of(Integer code) {
        return EnumUtils.of(StoreStatusEnum.class, code);
    }
}
