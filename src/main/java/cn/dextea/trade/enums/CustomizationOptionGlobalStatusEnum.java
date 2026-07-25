package cn.dextea.trade.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CustomizationOptionGlobalStatusEnum implements CodeEnum {

    DISABLED(0, "禁用"),
    ACTIVE(1, "激活");

    private final int code;
    private final String description;

    public static CustomizationOptionGlobalStatusEnum of(Integer code) {
        return EnumUtils.of(CustomizationOptionGlobalStatusEnum.class, code);
    }
}
