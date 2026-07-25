package cn.dextea.trade.mq;

import cn.dextea.trade.entity.Order;
import cn.dextea.trade.enums.TradeStatusEnum;
import cn.dextea.trade.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 支付回单消息处理实现。
 *
 * <p>核心流程：根据 {@code out_trade_no} 定位订单，按 {@code trade_status} 更新订单的「交易状态」
 * （{@link TradeStatusEnum}）。交易状态仅描述支付维度，与制作进度（{@link cn.dextea.trade.enums.MakingStatusEnum}）相互独立。
 * 通过「仅当订单处于待支付时更新为已支付」的乐观条件保证幂等，避免重复消费或覆盖终态订单。</p>
 *
 * <p>注意：回单消息来自支付平台经 RocketMQ 投递，平台侧已对原始异步通知做校验与解析；
 * 若需在本系统再次校验签名，可在此补充支付平台签名验签逻辑。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentNotifyServiceImpl implements PaymentNotifyService {

    private final OrderMapper orderMapper;

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
            TradeStatusEnum target = "TRADE_FINISHED".equals(tradeStatus)
                    ? TradeStatusEnum.TRADE_FINISHED : TradeStatusEnum.TRADE_PAID;
            markOrderPaid(outTradeNo, data.getTradeNo(), target, tradeStatus, message.getTraceId());
        } else if ("TRADE_CLOSED".equals(tradeStatus)) {
            markOrderClosed(outTradeNo, message.getTraceId());
        } else {
            // 非终态（如 WAIT_BUYER_PAY）或非支付成功状态，仅记录不更新，避免阻塞重试
            log.info("收到非终态/非支付成功的回单，忽略处理: outTradeNo={}, tradeStatus={}", outTradeNo, tradeStatus);
        }
    }

    private void markOrderPaid(String outTradeNo, String tradeNo, TradeStatusEnum target,
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

        int updated = orderMapper.markPaid(outTradeNo, tradeNo, target.getCode(), TradeStatusEnum.TRADE_WAIT_PAY.getCode());
        if (updated > 0) {
            log.info("订单支付状态更新成功: orderNo={}, tradeNo={}, tradeStatus={}, traceId={}",
                    outTradeNo, tradeNo, tradeStatus, traceId);
        } else {
            // 乐观条件未命中（并发或已被其他流程变更），再次确认当前状态
            Order latest = orderMapper.selectByOrderNo(outTradeNo);
            if (latest != null && isPaidTerminal(latest.getTradeStatus())) {
                log.info("订单已被其他处理更新为已支付，幂等跳过: orderNo={}", outTradeNo);
            } else if (latest != null && isClosedTerminal(latest.getTradeStatus())) {
                // 已被并发变更为其他终态（已退款/已关闭/退款中），属正常结果，不应告警
                log.info("订单已被并发变更为其他终态，跳过支付更新: orderNo={}, status={}", outTradeNo, latest.getTradeStatus());
            } else {
                log.warn("订单支付状态更新未生效，请核查: orderNo={}, currentStatus={}",
                        outTradeNo, latest == null ? "null" : latest.getTradeStatus());
            }
        }
    }

    private void markOrderClosed(String outTradeNo, String traceId) {
        Order order = orderMapper.selectByOrderNo(outTradeNo);
        if (order == null) {
            log.error("支付回单对应的订单不存在，忽略关闭: outTradeNo={}, traceId={}", outTradeNo, traceId);
            return;
        }
        Integer cur = order.getTradeStatus();
        if (isStatus(cur, TradeStatusEnum.TRADE_PAID)) {
            // 支付后全额退款导致的关闭（TRADE_CLOSED after paid），应记为已退款
            updateTradeStatus(outTradeNo, TradeStatusEnum.TRADE_REFUNDED, TradeStatusEnum.TRADE_PAID,
                    traceId, "订单全额退款完成（支付后关闭）");
        } else if (isStatus(cur, TradeStatusEnum.TRADE_FINISHED)) {
            updateTradeStatus(outTradeNo, TradeStatusEnum.TRADE_REFUNDED, TradeStatusEnum.TRADE_FINISHED,
                    traceId, "订单全额退款完成（支付后关闭）");
        } else if (isStatus(cur, TradeStatusEnum.TRADE_WAIT_PAY)) {
            // 未付款超时关闭
            updateTradeStatus(outTradeNo, TradeStatusEnum.TRADE_CLOSED, TradeStatusEnum.TRADE_WAIT_PAY,
                    traceId, "订单超时未支付已关闭");
        } else {
            // 已关闭/退款中/已退款，幂等跳过
            log.info("订单已处于退款/关闭终态，忽略关闭通知: orderNo={}, status={}, traceId={}", outTradeNo, cur, traceId);
        }
    }

    private void updateTradeStatus(String outTradeNo, TradeStatusEnum target, TradeStatusEnum expected,
                                   String traceId, String successMsg) {
        int updated = orderMapper.updateTradeStatusByOrderNo(outTradeNo, target.getCode(), expected.getCode());
        if (updated > 0) {
            log.info("{}: orderNo={}, traceId={}", successMsg, outTradeNo, traceId);
        } else {
            log.info("订单状态更新未生效（可能已被并发处理），忽略: orderNo={}, target={}, traceId={}",
                    outTradeNo, target.name(), traceId);
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
