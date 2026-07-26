package cn.dextea.trade.service.impl;

import cn.dextea.trade.entity.Order;
import cn.dextea.trade.enums.OrderEventEnum;
import cn.dextea.trade.enums.TradeStatusEnum;
import cn.dextea.trade.exception.BizError;
import cn.dextea.trade.mapper.OrderMapper;
import cn.dextea.trade.pay.domain.port.PaymentResultSyncPort;
import cn.dextea.trade.service.OrderStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 订单域对支付域 {@link PaymentResultSyncPort} 端口的适配器实现（原 PaymentNotifyServiceImpl 的订单状态流转部分）。
 *
 * <p>负责把支付结果映射为订单事件（PAY / PAY_AND_FINISH / REFUND / CLOSE），
 * 并委托 {@link OrderStatusService} 完成状态流转与幂等判定。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaymentSyncAdapter implements PaymentResultSyncPort {

    private final OrderMapper orderMapper;
    private final OrderStatusService orderStatusService;

    /**
     * 处理支付成功回单：委托 {@link OrderStatusService} 执行 待支付 → 已支付/已结算 流转。
     * <p>已是支付终态的订单直接跳过；CAS 失败（已被并发处理）也视为幂等跳过。</p>
     */
    @Override
    public void syncPaid(String orderNo, String tradeNo, boolean settled, String rawStatus, String traceId) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            log.error("支付回单对应的订单不存在，忽略: orderNo={}, traceId={}", orderNo, traceId);
            return;
        }

        // 已是支付终态（已支付/已结算/已退款），幂等跳过，不再尝试更新
        Integer cur = order.getTradeStatus();
        if (isPaidTerminal(cur)) {
            log.info("订单已处于支付终态，幂等跳过: orderNo={}, status={}", orderNo, cur);
            return;
        }

        // TRADE_FINISHED 表示交易已结算，与 TRADE_SUCCESS 同属支付成功，但语义不同；
        // 已结算仍支持随时退款（TRADE_FINISHED → TRADE_REFUNDED），退款窗口不关闭
        OrderEventEnum event = settled ? OrderEventEnum.PAY_AND_FINISH : OrderEventEnum.PAY;

        try {
            orderStatusService.changeStatus(
                    orderNo, event, "system-pay-callback",
                    tradeNo, LocalDateTime.now(), null
            );
            log.info("订单支付状态更新成功: orderNo={}, tradeNo={}, tradeStatus={}, traceId={}",
                    orderNo, tradeNo, rawStatus, traceId);
        } catch (BizError e) {
            // CAS 失败或流转非法：再次确认当前状态，已是终态则幂等跳过，否则告警
            Order latest = orderMapper.selectByOrderNo(orderNo);
            if (latest != null && (isPaidTerminal(latest.getTradeStatus()) || isClosedTerminal(latest.getTradeStatus()))) {
                log.info("订单已被并发处理为终态，幂等跳过: orderNo={}, status={}", orderNo, latest.getTradeStatus());
            } else {
                log.warn("订单支付状态更新未生效，请核查: orderNo={}, currentStatus={}, err={}",
                        orderNo, latest == null ? "null" : latest.getTradeStatus(), e.getMessage());
            }
        }
    }

    /**
     * 处理关闭回单：根据当前状态区分「未付款超时关闭」与「支付后全额退款关闭」。
     */
    @Override
    public void syncClosed(String orderNo, String traceId) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            log.error("支付回单对应的订单不存在，忽略关闭: orderNo={}, traceId={}", orderNo, traceId);
            return;
        }
        Integer cur = order.getTradeStatus();

        try {
            if (isStatus(cur, TradeStatusEnum.TRADE_PAID)) {
                // 支付后全额退款导致的关闭（TRADE_CLOSED after paid），应记为已退款
                orderStatusService.changeStatus(
                        orderNo, OrderEventEnum.REFUND, "system-pay-callback",
                        null, null, LocalDateTime.now()
                );
                log.info("订单全额退款完成（支付后关闭）: orderNo={}, traceId={}", orderNo, traceId);
            } else if (isStatus(cur, TradeStatusEnum.TRADE_FINISHED)) {
                // 已结算后全额退款导致的关闭，同样记为已退款
                orderStatusService.changeStatus(
                        orderNo, OrderEventEnum.REFUND, "system-pay-callback",
                        null, null, LocalDateTime.now()
                );
                log.info("订单全额退款完成（结算后关闭）: orderNo={}, traceId={}", orderNo, traceId);
            } else if (isStatus(cur, TradeStatusEnum.TRADE_WAIT_PAY)) {
                // 未付款超时关闭
                orderStatusService.changeStatus(orderNo, OrderEventEnum.CLOSE, "system-pay-callback");
                log.info("订单超时未支付已关闭: orderNo={}, traceId={}", orderNo, traceId);
            } else {
                // 已关闭/退款中/已退款，幂等跳过
                log.info("订单已处于退款/关闭终态，忽略关闭通知: orderNo={}, status={}, traceId={}", orderNo, cur, traceId);
            }
        } catch (BizError e) {
            log.info("订单关闭/退款状态更新未生效（可能已被并发处理），忽略: orderNo={}, target={}, err={}",
                    orderNo, cur, e.getMessage());
        }
    }

    private static boolean isPaidTerminal(Integer status) {
        return isStatus(status, TradeStatusEnum.TRADE_PAID, TradeStatusEnum.TRADE_FINISHED, TradeStatusEnum.TRADE_REFUNDED);
    }

    private static boolean isClosedTerminal(Integer status) {
        return isStatus(status, TradeStatusEnum.TRADE_CLOSED, TradeStatusEnum.TRADE_REFUNDED, TradeStatusEnum.TRADE_REFUNDING);
    }

    /**
     * null 安全判断：actual 是否等于给定枚举之一。
     *
     * <p>注意：{@link TradeStatusEnum#getCode()} 返回基本类型 int，不可反向调用 {@code equals}，
     * 故以 {@code Integer} 状态的 {@code equals} 为主比较，避免编译错误与空指针。</p>
     */
    private static boolean isStatus(Integer actual, TradeStatusEnum... expected) {
        if (actual == null) {
            return false;
        }
        for (TradeStatusEnum e : expected) {
            if (actual.equals(e.getCode())) {
                return true;
            }
        }
        return false;
    }
}
