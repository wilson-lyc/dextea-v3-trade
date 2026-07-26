package cn.dextea.trade.catalog.domain.enums;

import cn.dextea.trade.common.enums.CodeEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductStoreStatusEnum implements CodeEnum {

    SOLD_OUT(0, "售罄"),
    AVAILABLE(1, "可售");

    private final int code;
    private final String description;

    public static ProductStoreStatusEnum of(Integer code) {
        return cn.dextea.trade.common.enums.EnumUtils.of(ProductStoreStatusEnum.class, code);
    }
}
