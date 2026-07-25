package cn.dextea.trade.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 交易（支付）状态枚举，与支付平台回单 trade_status 对齐。
 *
 * <p>命名统一以 {@code TRADE_} 开头，便于与制作进度状态（{@link MakingStatusEnum}）区分。
 * 与支付宝/微信异步通知的语义对应关系：
 * <ul>
 *     <li>{@link #TRADE_WAIT_PAY} —— WAIT_BUYER_PAY（等待买家付款）</li>
 *     <li>{@link #TRADE_PAID} —— TRADE_SUCCESS（支付成功）</li>
 *     <li>{@link #TRADE_FINISHED} —— TRADE_FINISHED（交易完成/已结算，退款窗口关闭）</li>
 *     <li>{@link #TRADE_CLOSED} —— TRADE_CLOSED（未付款超时关闭）</li>
 *     <li>{@link #TRADE_REFUNDING} —— 退款处理中（退款通知触发）</li>
 *     <li>{@link #TRADE_REFUNDED} —— 全额退款完成（含支付后全额退款导致的 TRADE_CLOSED）</li>
 * </ul>
 */
@Getter
@RequiredArgsConstructor
public enum TradeStatusEnum {

    TRADE_WAIT_PAY(0, "待支付"),
    TRADE_PAID(1, "已支付"),
    TRADE_FINISHED(2, "已结算"),
    TRADE_CLOSED(3, "已关闭"),
    TRADE_REFUNDING(4, "退款中"),
    TRADE_REFUNDED(5, "已退款");

    private final int code;
    private final String description;

    public static TradeStatusEnum of(Integer code) {
        if (code == null) {
            throw new IllegalArgumentException("未知交易状态: null");
        }
        for (TradeStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("未知交易状态: " + code);
    }
}
