package cn.dextea.trade.order.application.service;

import cn.dextea.trade.order.domain.dto.QueryTradeResult;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.port.PaymentPort;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentReconciliationService {

    private final OrderRepository orderRepository;
    private final PaymentPort paymentPort;

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

        if (tradeResult == null || !tradeResult.isPaidStatus()) {
            log.info("支付渠道交易仍未支付成功, 维持本地支付状态, orderId={}, orderNo={}, tradeStatus={}",
                    order.getId(), order.getOrderNo(),
                    tradeResult == null ? null : tradeResult.getTradeStatus());
            return;
        }

        // 优先采用支付宝返回的买家付款时间, 缺失时才退化为当前时间
        LocalDateTime paidAt = tradeResult.getPaidAt() == null ? LocalDateTime.now() : tradeResult.getPaidAt();
        order.markPaid(paidAt);
        try {
            orderRepository.updatePaymentStatus(order);
            log.info("主动对账发现订单已支付, 已回写本地, orderId={}, orderNo={}, tradeNo={}, paidAt={}",
                    order.getId(), order.getOrderNo(), tradeResult.getTradeNo(), paidAt);
        } catch (RuntimeException e) {
            // 与支付回调并发更新导致乐观锁冲突：说明本地已被回调置为已支付，结果一致，无需重试
            log.warn("主动对账回写支付状态冲突, 视为已被支付回调更新, orderId={}, orderNo={}",
                    order.getId(), order.getOrderNo());
        }
    }
}
