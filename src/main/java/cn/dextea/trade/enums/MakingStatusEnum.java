package cn.dextea.trade.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 制作进度状态枚举，描述门店侧订单的制作与交付过程，与支付状态（{@link TradeStatusEnum}）相互独立。
 */
@Getter
@RequiredArgsConstructor
public enum MakingStatusEnum {

    MAKING_WAIT(0, "待制作"),
    MAKING_DOING(1, "制作中"),
    MAKING_DONE(2, "制作完成"),
    MAKING_DELIVERED(3, "已交付");

    private final int code;
    private final String description;

    public static MakingStatusEnum of(Integer code) {
        if (code == null) {
            throw new IllegalArgumentException("未知制作状态: null");
        }
        for (MakingStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("未知制作状态: " + code);
    }
}
