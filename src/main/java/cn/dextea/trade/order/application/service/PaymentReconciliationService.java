package cn.dextea.trade.order.application.service;

import cn.dextea.trade.order.domain.dto.QueryTradeResult;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.port.PaymentPort;
import cn.dextea.trade.order.domain.service.OrderPaymentService;
import cn.dextea.trade.order.domain.service.PickupCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentReconciliationService {

    private final PaymentPort paymentPort;
    private final OrderPaymentService orderPaymentService;
    private final PickupCodeGenerator pickupCodeGenerator;

    public void reconcileIfPending(Order order) {
        if (order == null || !order.isPendingPayment()) {
            return;
        }
        QueryTradeResult tradeResult;
        try {
            tradeResult = paymentPort.queryTrade(order.getOrderNo());
        } catch (Exception e) {
            log.warn("主动查询支付渠道交易失败, 降级返回本地支付状态, orderId={}, orderNo={}, localPaymentStatus={}",
                    order.getId(), order.getOrderNo(), order.getPaymentStatus(), e);
            return;
        }

        if (tradeResult == null) {
            return;
        }

        if (tradeResult.isPaidStatus()) {
            reconcilePaid(order, tradeResult);
            return;
        }

        if (tradeResult.isClosedStatus()) {
            reconcileClosed(order);
            return;
        }

        log.info("支付渠道交易未支付也未关闭, 维持本地支付状态, orderId={}, orderNo={}, tradeStatus={}",
                order.getId(), order.getOrderNo(), tradeResult.getTradeStatus());
    }

    private void reconcilePaid(Order order, QueryTradeResult tradeResult) {
        // 优先采用支付宝返回的买家付款时间, 缺失时才退化为当前时间
        LocalDateTime paidAt = tradeResult.getPaidAt() == null ? LocalDateTime.now() : tradeResult.getPaidAt();
        String pickupCode = pickupCodeGenerator.generate(order.getStoreId(), LocalDate.now());
        orderPaymentService.markPaid(order, paidAt, tradeResult.getTradeNo(), pickupCode);
    }

    private void reconcileClosed(Order order) {
        // 支付渠道侧交易关闭：订单不再提供手动取消, 统一按支付超时处理
        orderPaymentService.markTimeout(order);
    }
}
