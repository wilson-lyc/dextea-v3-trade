package cn.dextea.trade.order.infrastructure.adapter;

import cn.dextea.trade.exception.BizError;
import cn.dextea.trade.order.domain.enums.OrderEventEnum;
import cn.dextea.trade.order.domain.enums.TradeStatusEnum;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.port.OrderRepository;
import cn.dextea.trade.order.domain.service.OrderStatusDomainService;
import cn.dextea.trade.pay.domain.port.PaymentResultSyncPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 订单域对支付域 {@link PaymentResultSyncPort} 端口的适配器实现（原 OrderPaymentSyncAdapter）。
 *
 * <p>负责把支付结果映射为订单事件（PAY / PAY_AND_FINISH / REFUND / CLOSE），
 * 并委托 {@link OrderStatusDomainService} 完成状态流转与幂等判定。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaymentSyncAdapter implements PaymentResultSyncPort {

    private final OrderRepository orderRepository;
    private final OrderStatusDomainService orderStatusDomainService;

    @Override
    public void syncPaid(String orderNo, String tradeNo, boolean settled, String rawStatus, String traceId) {
        Order order = orderRepository.findByOrderNo(orderNo);
        if (order == null) {
            log.error("支付回单对应的订单不存在，忽略: orderNo={}, traceId={}", orderNo, traceId);
            return;
        }

        Integer cur = order.getTradeStatus();
        if (isPaidTerminal(cur)) {
            log.info("订单已处于支付终态，幂等跳过: orderNo={}, status={}", orderNo, cur);
            return;
        }

        OrderEventEnum event = settled ? OrderEventEnum.PAY_AND_FINISH : OrderEventEnum.PAY;

        try {
            orderStatusDomainService.changeStatus(
                    orderNo, event, "system-pay-callback",
                    tradeNo, LocalDateTime.now(), null
            );
            log.info("订单支付状态更新成功: orderNo={}, tradeNo={}, tradeStatus={}, traceId={}",
                    orderNo, tradeNo, rawStatus, traceId);
        } catch (BizError e) {
            Order latest = orderRepository.findByOrderNo(orderNo);
            if (latest != null && (isPaidTerminal(latest.getTradeStatus()) || isClosedTerminal(latest.getTradeStatus()))) {
                log.info("订单已被并发处理为终态，幂等跳过: orderNo={}, status={}", orderNo, latest.getTradeStatus());
            } else {
                log.warn("订单支付状态更新未生效，请核查: orderNo={}, currentStatus={}, err={}",
                        orderNo, latest == null ? "null" : latest.getTradeStatus(), e.getMessage());
            }
        }
    }

    @Override
    public void syncClosed(String orderNo, String traceId) {
        Order order = orderRepository.findByOrderNo(orderNo);
        if (order == null) {
            log.error("支付回单对应的订单不存在，忽略关闭: orderNo={}, traceId={}", orderNo, traceId);
            return;
        }
        Integer cur = order.getTradeStatus();

        try {
            if (isStatus(cur, TradeStatusEnum.TRADE_PAID)) {
                orderStatusDomainService.changeStatus(
                        orderNo, OrderEventEnum.REFUND, "system-pay-callback",
                        null, null, LocalDateTime.now()
                );
                log.info("订单全额退款完成（支付后关闭）: orderNo={}, traceId={}", orderNo, traceId);
            } else if (isStatus(cur, TradeStatusEnum.TRADE_FINISHED)) {
                orderStatusDomainService.changeStatus(
                        orderNo, OrderEventEnum.REFUND, "system-pay-callback",
                        null, null, LocalDateTime.now()
                );
                log.info("订单全额退款完成（结算后关闭）: orderNo={}, traceId={}", orderNo, traceId);
            } else if (isStatus(cur, TradeStatusEnum.TRADE_WAIT_PAY)) {
                orderStatusDomainService.changeStatus(orderNo, OrderEventEnum.CLOSE, "system-pay-callback", null, null, null);
                log.info("订单超时未支付已关闭: orderNo={}, traceId={}", orderNo, traceId);
            } else {
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
