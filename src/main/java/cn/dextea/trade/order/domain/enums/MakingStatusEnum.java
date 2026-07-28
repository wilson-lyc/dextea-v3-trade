package cn.dextea.trade.order.domain.enums;
import cn.dextea.trade.common.enums.CodeEnum;
import cn.dextea.trade.common.enums.EnumUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
@Getter
@RequiredArgsConstructor
public enum MakingStatusEnum implements CodeEnum {
    MAKING_WAIT(0, "待制作"),
    MAKING_DOING(1, "制作中"),
    MAKING_DONE(2, "制作完成"),
    MAKING_DELIVERED(3, "已交付");
    private final int code;
    private final String description;
    public static MakingStatusEnum of(Integer code) {
        return EnumUtils.of(MakingStatusEnum.class, code);
    }
}
