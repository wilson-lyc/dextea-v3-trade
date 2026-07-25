package cn.dextea.trade.statemachine;

import cn.dextea.trade.enums.OrderEventEnum;
import cn.dextea.trade.enums.TradeStatusEnum;

import java.util.EnumMap;
import java.util.Map;

/**
 * 订单交易状态流转规则表（状态机白名单）。
 *
 * <p>以 {@code (当前状态, 事件) → 目标状态} 的映射定义所有合法流转路径。
 * 不在表中的组合视为非法流转，{@link #getTarget} 返回 {@code null}，
 * 由调用方拒绝执行，从根本上杜绝「已支付倒退到待支付」等逆向操作。</p>
 *
 * <p>当前定义的合法路径：</p>
 * <pre>
 *   WAIT_PAY  + PAY             → PAID
 *   WAIT_PAY  + PAY_AND_FINISH  → FINISHED
 *   WAIT_PAY  + CLOSE           → CLOSED
 *   PAID      + REFUND          → REFUNDED
 *   FINISHED  + REFUND          → REFUNDED
 * </pre>
 *
 * <p>新增状态或事件时，只需在此表追加条目，所有状态变更入口自动生效，
 * 无需修改散落在各 Service 中的判断分支。</p>
 */
public final class TransitionRules {

    private static final Map<TradeStatusEnum, Map<OrderEventEnum, TradeStatusEnum>> RULES = new EnumMap<>(TradeStatusEnum.class);

    static {
        // 待支付 → 已支付 / 已结算 / 已关闭
        Map<OrderEventEnum, TradeStatusEnum> fromWaitPay = new EnumMap<>(OrderEventEnum.class);
        fromWaitPay.put(OrderEventEnum.PAY, TradeStatusEnum.TRADE_PAID);
        fromWaitPay.put(OrderEventEnum.PAY_AND_FINISH, TradeStatusEnum.TRADE_FINISHED);
        fromWaitPay.put(OrderEventEnum.CLOSE, TradeStatusEnum.TRADE_CLOSED);
        RULES.put(TradeStatusEnum.TRADE_WAIT_PAY, fromWaitPay);

        // 已支付 → 已退款
        Map<OrderEventEnum, TradeStatusEnum> fromPaid = new EnumMap<>(OrderEventEnum.class);
        fromPaid.put(OrderEventEnum.REFUND, TradeStatusEnum.TRADE_REFUNDED);
        RULES.put(TradeStatusEnum.TRADE_PAID, fromPaid);

        // 已结算 → 已退款
        Map<OrderEventEnum, TradeStatusEnum> fromFinished = new EnumMap<>(OrderEventEnum.class);
        fromFinished.put(OrderEventEnum.REFUND, TradeStatusEnum.TRADE_REFUNDED);
        RULES.put(TradeStatusEnum.TRADE_FINISHED, fromFinished);
    }

    private TransitionRules() {
    }

    /**
     * 查询目标状态。
     *
     * @param from   当前状态
     * @param event  触发事件
     * @return 目标状态；若 {@code (from, event)} 不在白名单中则返回 {@code null}，表示非法流转
     */
    public static TradeStatusEnum getTarget(TradeStatusEnum from, OrderEventEnum event) {
        if (from == null || event == null) {
            return null;
        }
        Map<OrderEventEnum, TradeStatusEnum> transitions = RULES.get(from);
        if (transitions == null) {
            return null;
        }
        return transitions.get(event);
    }
}
