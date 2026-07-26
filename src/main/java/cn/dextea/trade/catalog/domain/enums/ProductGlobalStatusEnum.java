package cn.dextea.trade.catalog.domain.enums;

import cn.dextea.trade.enums.CodeEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductGlobalStatusEnum implements CodeEnum {

    OFF_SHELF(0, "下架"),
    ON_SHELF(1, "上架");

    private final int code;
    private final String description;

    public static ProductGlobalStatusEnum of(Integer code) {
        return cn.dextea.trade.enums.EnumUtils.of(ProductGlobalStatusEnum.class, code);
    }
}
