package cn.dextea.trade.catalog.domain.enums;

import cn.dextea.trade.common.enums.CodeEnum;
import cn.dextea.trade.common.enums.EnumUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductStoreStatusEnum implements CodeEnum {

    UNAVAILABLE(0, "门店不可售"),
    AVAILABLE(1, "门店可售");

    private final int code;
    private final String description;

    public static ProductStoreStatusEnum of(Integer code) {
        return EnumUtils.of(ProductStoreStatusEnum.class, code);
    }
}
