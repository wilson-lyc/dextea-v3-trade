package cn.dextea.trade.mq;

import cn.dextea.trade.entity.Order;
import cn.dextea.trade.enums.OrderEventEnum;
import cn.dextea.trade.enums.TradeStatusEnum;
import cn.dextea.trade.exception.BizError;
import cn.dextea.trade.mapper.OrderMapper;
import cn.dextea.trade.service.OrderStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 支付回单消息处理实现。
 *
 * <p>核心流程：根据 {@code out_trade_no} 定位订单，按 {@code trade_status} 映射为
 * {@link OrderEventEnum} 事件，委托 {@link OrderStatusService#changeStatus} 统一变更状态。
 * 状态变更的三层防护（Redis 锁 + 状态机白名单 + 数据库 CAS）全部封装在
 * {@code OrderStatusService} 内，本类只负责「回单 → 事件」的语义映射。</p>
 *
 * <p>幂等保障：{@link OrderStatusService} 内部 CAS 条件 {@code WHERE trade_status=? AND version=?}
 * 保证只有前置状态匹配时才更新；已是终态的订单 CAS 返回 0，本类捕获后视为幂等跳过，
 * 不再抛异常触发 MQ 重试。</p>
 *
 * <p>注意：回单消息来自支付平台经 RocketMQ 投递，平台侧已对原始异步通知做校验与解析；
 * 若需在本系统再次校验签名，可在此补充支付平台签名验签逻辑。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentNotifyServiceImpl implements PaymentNotifyService {

    private final OrderMapper orderMapper;
    private final OrderStatusService orderStatusService;

    @Override
    public void handleNotify(PaymentNotifyMessage message) {
        if (message == null || message.getData() == null) {
            log.error("支付回单消息缺失 data 字段，忽略: message={}", message);
            return;
        }
        PaymentNotifyData data = message.getData();
        String outTradeNo = data.getOutTradeNo();
        if (outTradeNo == null || outTradeNo.isBlank()) {
            log.error("支付回单缺少 out_trade_no，忽略: traceId={}", message.getTraceId());
            return;
        }

        String tradeStatus = data.getTradeStatus();
        if (isPaid(tradeStatus)) {
            // TRADE_FINISHED 表示交易已结算（退款窗口关闭），与 TRADE_SUCCESS 同属支付成功，但语义不同
            OrderEventEnum event = "TRADE_FINISHED".equals(tradeStatus)
                    ? OrderEventEnum.PAY_AND_FINISH : OrderEventEnum.PAY;
            markOrderPaid(outTradeNo, data.getTradeNo(), event, tradeStatus, message.getTraceId());
        } else if ("TRADE_CLOSED".equals(tradeStatus)) {
            markOrderClosed(outTradeNo, message.getTraceId());
        } else {
            // 非终态（如 WAIT_BUYER_PAY）或非支付成功状态，仅记录不更新，避免阻塞重试
            log.info("收到非终态/非支付成功的回单，忽略处理: outTradeNo={}, tradeStatus={}", outTradeNo, tradeStatus);
        }
    }

    /**
     * 处理支付成功回单：委托 {@link OrderStatusService} 执行 待支付 → 已支付/已结算 流转。
     * <p>已是支付终态的订单直接跳过；CAS 失败（已被并发处理）也视为幂等跳过。</p>
     */
    private void markOrderPaid(String outTradeNo, String tradeNo, OrderEventEnum event,
                               String tradeStatus, String traceId) {
        Order order = orderMapper.selectByOrderNo(outTradeNo);
        if (order == null) {
            log.error("支付回单对应的订单不存在，忽略: outTradeNo={}, traceId={}", outTradeNo, traceId);
            return;
        }

        // 已是支付终态（已支付/已结算/已退款），幂等跳过，不再尝试更新
        Integer cur = order.getTradeStatus();
        if (isPaidTerminal(cur)) {
            log.info("订单已处于支付终态，幂等跳过: orderNo={}, status={}", outTradeNo, cur);
            return;
        }

        try {
            orderStatusService.changeStatus(
                    outTradeNo, event, "system-pay-callback",
                    tradeNo, LocalDateTime.now(), null
            );
            log.info("订单支付状态更新成功: orderNo={}, tradeNo={}, tradeStatus={}, traceId={}",
                    outTradeNo, tradeNo, tradeStatus, traceId);
        } catch (BizError e) {
            // CAS 失败或流转非法：再次确认当前状态，已是终态则幂等跳过，否则告警
            Order latest = orderMapper.selectByOrderNo(outTradeNo);
            if (latest != null && (isPaidTerminal(latest.getTradeStatus()) || isClosedTerminal(latest.getTradeStatus()))) {
                log.info("订单已被并发处理为终态，幂等跳过: orderNo={}, status={}", outTradeNo, latest.getTradeStatus());
            } else {
                log.warn("订单支付状态更新未生效，请核查: orderNo={}, currentStatus={}, err={}",
                        outTradeNo, latest == null ? "null" : latest.getTradeStatus(), e.getMessage());
            }
        }
    }

    /**
     * 处理关闭回单：根据当前状态区分「未付款超时关闭」与「支付后全额退款关闭」。
     */
    private void markOrderClosed(String outTradeNo, String traceId) {
        Order order = orderMapper.selectByOrderNo(outTradeNo);
        if (order == null) {
            log.error("支付回单对应的订单不存在，忽略关闭: outTradeNo={}, traceId={}", outTradeNo, traceId);
            return;
        }
        Integer cur = order.getTradeStatus();

        try {
            if (isStatus(cur, TradeStatusEnum.TRADE_PAID)) {
                // 支付后全额退款导致的关闭（TRADE_CLOSED after paid），应记为已退款
                orderStatusService.changeStatus(
                        outTradeNo, OrderEventEnum.REFUND, "system-pay-callback",
                        null, null, LocalDateTime.now()
                );
                log.info("订单全额退款完成（支付后关闭）: orderNo={}, traceId={}", outTradeNo, traceId);
            } else if (isStatus(cur, TradeStatusEnum.TRADE_FINISHED)) {
                // 已结算后全额退款导致的关闭，同样记为已退款
                orderStatusService.changeStatus(
                        outTradeNo, OrderEventEnum.REFUND, "system-pay-callback",
                        null, null, LocalDateTime.now()
                );
                log.info("订单全额退款完成（结算后关闭）: orderNo={}, traceId={}", outTradeNo, traceId);
            } else if (isStatus(cur, TradeStatusEnum.TRADE_WAIT_PAY)) {
                // 未付款超时关闭
                orderStatusService.changeStatus(outTradeNo, OrderEventEnum.CLOSE, "system-pay-callback");
                log.info("订单超时未支付已关闭: orderNo={}, traceId={}", outTradeNo, traceId);
            } else {
                // 已关闭/退款中/已退款，幂等跳过
                log.info("订单已处于退款/关闭终态，忽略关闭通知: orderNo={}, status={}, traceId={}", outTradeNo, cur, traceId);
            }
        } catch (BizError e) {
            log.info("订单关闭/退款状态更新未生效（可能已被并发处理），忽略: orderNo={}, target={}, err={}",
                    outTradeNo, cur, e.getMessage());
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

    private static boolean isPaid(String tradeStatus) {
        return "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus);
    }
}
