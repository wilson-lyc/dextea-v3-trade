package cn.dextea.trade.order.domain.service;

import cn.dextea.trade.order.domain.enums.OrderEventEnum;
import cn.dextea.trade.order.domain.enums.TradeStatusEnum;

import java.util.EnumMap;
import java.util.Map;

/**
 * 订单交易状态机规则（原 {@code TradeStatusTransitionRules}）。
 *
 * <p>以「当前状态 + 触发事件 → 目标状态」的白名单描述合法流转，不在白名单中的组合视为非法。</p>
 */
public final class OrderStatusMachine {

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

    private OrderStatusMachine() {
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
