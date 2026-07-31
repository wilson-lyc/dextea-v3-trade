package cn.dextea.trade.order.infrastructure.gateway.impl;

import cn.dextea.trade.common.error.BizError;
import cn.dextea.trade.order.domain.model.aggregate.Order;
import cn.dextea.trade.order.domain.model.valueobject.PaymentStatus;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.order.domain.service.OrderStatusDomainService;
import cn.dextea.trade.pay.domain.gateway.PaymentResultSyncGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaymentSyncAdapter implements PaymentResultSyncGateway {
    private final OrderRepository orderRepository;
    private final OrderStatusDomainService orderStatusDomainService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncPaid(String orderNo, String tradeNo, String rawStatus, String traceId) {
        Order order = orderRepository.findByOrderNo(orderNo);
        if (order == null) {
            log.error("支付回单对应的订单不存在，忽略: orderNo={}, traceId={}", orderNo, traceId);
            return;
        }
        Integer cur = order.getPaymentStatus().getCode();
        if (isPaidTerminal(cur)) {
            log.info("订单已处于支付终态，幂等跳过: orderNo={}, status={}", orderNo, cur);
            return;
        }
        try {
            orderStatusDomainService.markPaid(orderNo, tradeNo, LocalDateTime.now(), "system-pay-callback");
            log.info("订单支付状态更新成功: orderNo={}, tradeNo={}, tradeStatus={}, traceId={}",
                    orderNo, tradeNo, rawStatus, traceId);
        } catch (BizError e) {
            Order latest = orderRepository.findByOrderNo(orderNo);
            if (latest != null && (isPaidTerminal(latest.getPaymentStatus().getCode())
                    || isClosedTerminal(latest.getPaymentStatus().getCode()))) {
                log.info("订单已被并发处理为终态，幂等跳过: orderNo={}, status={}", orderNo, latest.getPaymentStatus().getCode());
            } else {
                log.warn("订单支付状态更新未生效，请核查: orderNo={}, currentStatus={}, err={}",
                        orderNo, latest == null ? "null" : latest.getPaymentStatus().getCode(), e.getMessage());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncClosed(String orderNo, String traceId) {
        Order order = orderRepository.findByOrderNo(orderNo);
        if (order == null) {
            log.error("支付回单对应的订单不存在，忽略关闭: orderNo={}, traceId={}", orderNo, traceId);
            return;
        }
        Integer cur = order.getPaymentStatus().getCode();
        try {
            if (isStatus(cur, PaymentStatus.PAID)) {
                orderStatusDomainService.markRefunded(orderNo, LocalDateTime.now(), "system-pay-callback");
                log.info("订单全额退款完成（支付后关闭）: orderNo={}, traceId={}", orderNo, traceId);
            } else if (isStatus(cur, PaymentStatus.PENDING)) {
                orderStatusDomainService.markPayTimeout(orderNo, "system-pay-callback");
                log.info("订单超时未支付已关闭: orderNo={}, traceId={}", orderNo, traceId);
            } else {
                log.info("订单已处于退款/超时终态，忽略关闭通知: orderNo={}, status={}, traceId={}", orderNo, cur, traceId);
            }
        } catch (BizError e) {
            log.info("订单关闭/退款状态更新未生效（可能已被并发处理），忽略: orderNo={}, target={}, err={}",
                    orderNo, cur, e.getMessage());
        }
    }

    private static boolean isPaidTerminal(Integer status) {
        return isStatus(status, PaymentStatus.PAID, PaymentStatus.REFUNDED);
    }

    private static boolean isClosedTerminal(Integer status) {
        return isStatus(status, PaymentStatus.PAY_TIMEOUT, PaymentStatus.REFUNDED, PaymentStatus.REFUNDING);
    }

    private static boolean isStatus(Integer actual, PaymentStatus... expected) {
        if (actual == null) {
            return false;
        }
        for (PaymentStatus e : expected) {
            if (actual.equals(e.getCode())) {
                return true;
            }
        }
        return false;
    }
}
