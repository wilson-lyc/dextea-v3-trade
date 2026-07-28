package cn.dextea.trade.order.domain.enums;
import cn.dextea.trade.common.enums.CodeEnum;
import cn.dextea.trade.common.enums.EnumUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
@Getter
@RequiredArgsConstructor
public enum CustomizationOptionStoreStatusEnum implements CodeEnum {
    UNAVAILABLE(0, "门店不可用"),
    AVAILABLE(1, "门店可用");
    private final int code;
    private final String description;
    public static CustomizationOptionStoreStatusEnum of(Integer code) {
        return EnumUtils.of(CustomizationOptionStoreStatusEnum.class, code);
    }
}
