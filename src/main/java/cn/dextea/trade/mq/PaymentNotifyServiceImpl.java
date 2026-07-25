package cn.dextea.trade.mq;

import cn.dextea.trade.entity.Order;
import cn.dextea.trade.enums.OrderStatusEnum;
import cn.dextea.trade.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 支付回单消息处理实现。
 *
 * <p>核心流程：根据 {@code out_trade_no} 定位订单，按 {@code trade_status} 更新订单状态。
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
            markOrderPaid(outTradeNo, data.getTradeNo(), tradeStatus, message.getTraceId());
        } else if ("TRADE_CLOSED".equals(tradeStatus)) {
            markOrderClosed(outTradeNo, message.getTraceId());
        } else {
            // 非终态（如 WAIT_BUYER_PAY）或非支付成功状态，仅记录不更新，避免阻塞重试
            log.info("收到非终态/非支付成功的回单，忽略处理: outTradeNo={}, tradeStatus={}", outTradeNo, tradeStatus);
        }
    }

    private void markOrderPaid(String outTradeNo, String tradeNo, String tradeStatus, String traceId) {
        Order order = orderMapper.selectByOrderNo(outTradeNo);
        if (order == null) {
            log.error("支付回单对应的订单不存在，忽略: outTradeNo={}, traceId={}", outTradeNo, traceId);
            return;
        }

        // 已是终态（已支付/已退款），幂等跳过
        Integer status = order.getStatus();
        if (OrderStatusEnum.PAID.getCode().equals(status) || OrderStatusEnum.REFUNDED.getCode().equals(status)) {
            log.info("订单已处于终态，幂等跳过: orderNo={}, status={}", outTradeNo, status);
            return;
        }

        int updated = orderMapper.markPaid(outTradeNo, tradeNo,
                OrderStatusEnum.PAID.getCode(), OrderStatusEnum.PENDING.getCode());
        if (updated > 0) {
            log.info("订单支付状态更新成功: orderNo={}, tradeNo={}, tradeStatus={}, traceId={}",
                    outTradeNo, tradeNo, tradeStatus, traceId);
        } else {
            // 乐观条件未命中（并发或已被其他流程变更），再次确认是否已支付
            Order latest = orderMapper.selectByOrderNo(outTradeNo);
            if (latest != null && OrderStatusEnum.PAID.getCode().equals(latest.getStatus())) {
                log.info("订单已被其他处理更新为已支付，幂等跳过: orderNo={}", outTradeNo);
            } else {
                log.warn("订单支付状态更新未生效，请核查: orderNo={}, currentStatus={}",
                        outTradeNo, latest == null ? "null" : latest.getStatus());
            }
        }
    }

    private void markOrderClosed(String outTradeNo, String traceId) {
        int updated = orderMapper.updateStatusByOrderNo(outTradeNo,
                OrderStatusEnum.CLOSED.getCode(), OrderStatusEnum.PENDING.getCode());
        if (updated > 0) {
            log.info("订单已关闭: orderNo={}, traceId={}", outTradeNo, traceId);
        } else {
            log.info("订单关闭未生效（可能已支付或已被处理），忽略: orderNo={}, traceId={}", outTradeNo, traceId);
        }
    }

    private static boolean isPaid(String tradeStatus) {
        return "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus);
    }
}
