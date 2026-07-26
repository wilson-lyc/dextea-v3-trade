package cn.dextea.trade.catalog.domain.enums;

import cn.dextea.trade.common.enums.CodeEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CustomerStatusEnum implements CodeEnum {

    INACTIVE(0, "未激活"),
    ACTIVE(1, "激活");

    private final int code;
    private final String description;

    public static CustomerStatusEnum of(Integer code) {
        return cn.dextea.trade.common.enums.EnumUtils.of(CustomerStatusEnum.class, code);
    }
}
