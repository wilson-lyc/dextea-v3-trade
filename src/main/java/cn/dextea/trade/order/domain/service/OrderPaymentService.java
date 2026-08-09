package cn.dextea.trade.order.domain.service;

import cn.dextea.trade.order.domain.enumeration.MakingStatus;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.port.MakingStatusPublisher;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPaymentService {

    private final OrderRepository orderRepository;
    private final PickupCodeGenerator pickupCodeGenerator;
    private final MakingStatusPublisher makingStatusPublisher;

    public void markPaid(Order order, LocalDateTime paidAt, String tradeNo) {
        if (order.isPaid()) {
            log.info("订单已是已支付状态, 忽略重复支付更新, orderNo={}", order.getOrderNo());
            return;
        }
        if (tradeNo != null) {
            order.setTradeNoIfAbsent(tradeNo);
        }
        MakingStatus fromMakingStatus = order.getMakingStatus();
        order.markPaid(paidAt);
        String pickupCode = pickupCodeGenerator.generate(order.getStoreId(), LocalDate.now());
        order.assignPickupCode(pickupCode);
        try {
            orderRepository.updatePaymentStatus(order);
            log.info("订单已标记为已支付, orderNo={}, paidAt={}, pickupCode={}, tradeNo={}",
                    order.getOrderNo(), paidAt, pickupCode, order.getTradeNo());
        } catch (RuntimeException e) {
            // 与支付回调/对账并发更新导致乐观锁冲突：本地已被对方更新为已支付，结果一致，无需重试
            log.warn("回写支付状态冲突, 视为已被并发更新, orderNo={}", order.getOrderNo());
            return;
        }
        if (fromMakingStatus == MakingStatus.PENDING) {
            makingStatusPublisher.publishMakingStatusChange(order.getOrderNo(), fromMakingStatus, MakingStatus.PREPARING);
        }
    }

    public void markTimeout(Order order) {
        if (!order.canMarkPaymentTimeout()) {
            log.info("订单当前状态不允许标记为支付超时, 忽略本次更新, orderNo={}, paymentStatus={}",
                    order.getOrderNo(), order.getPaymentStatus());
            return;
        }
        order.markPaymentTimeout();
        boolean updated = orderRepository.timeoutOrder(order);
        if (!updated) {
            log.info("订单支付状态已被并发变更, 放弃标记支付超时, orderNo={}", order.getOrderNo());
            return;
        }
        log.info("订单已标记为支付超时, orderNo={}, paymentExpiredAt={}", order.getOrderNo(), order.getPaymentExpiredAt());
    }

    public void cancel(Order order) {
        if (!order.canMarkCancelled()) {
            log.info("订单当前状态不允许标记为取消, 忽略本次更新, orderNo={}, paymentStatus={}",
                    order.getOrderNo(), order.getPaymentStatus());
            return;
        }
        order.markCancelled();
        try {
            orderRepository.cancelOrder(order);
            log.info("订单已标记为取消, orderNo={}", order.getOrderNo());
        } catch (RuntimeException e) {
            // 与并发更新冲突：本地已被对方更新，结果一致，无需重试
            log.warn("回写取消状态冲突, 视为已被并发更新, orderNo={}", order.getOrderNo());
        }
    }
}
