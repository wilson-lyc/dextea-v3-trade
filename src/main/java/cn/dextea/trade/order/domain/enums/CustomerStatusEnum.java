package cn.dextea.trade.order.domain.enums;
import cn.dextea.trade.common.enums.CodeEnum;
import cn.dextea.trade.common.enums.EnumUtils;
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
        return EnumUtils.of(CustomerStatusEnum.class, code);
    }
}
