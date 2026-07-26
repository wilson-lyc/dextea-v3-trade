package cn.dextea.trade.order.domain.enums;

import cn.dextea.trade.enums.CodeEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DiningMethodEnum implements CodeEnum {

    DINE_IN(0, "堂食"),
    TAKEOUT(1, "外带");

    private final int code;
    private final String description;

    public static DiningMethodEnum of(Integer code) {
        return cn.dextea.trade.enums.EnumUtils.of(DiningMethodEnum.class, code);
    }
}
