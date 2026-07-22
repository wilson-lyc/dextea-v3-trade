package cn.dextea.trade.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 订单状态枚举，对应 {@code orders.status} 字段（tinyint）。
 */
@Getter
@RequiredArgsConstructor
public enum OrderStatusEnum {

    /** 待支付 */
    PENDING(0, "待支付"),
    /** 已支付 */
    PAID(1, "已支付"),
    /** 已退款 */
    REFUNDED(2, "已退款"),
    /** 已关闭/已取消 */
    CLOSED(3, "已关闭");

    private final int code;
    private final String description;

    public static OrderStatusEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrderStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知订单状态: " + code);
    }
}
