package cn.dextea.trade.order.domain.service;

import cn.dextea.trade.order.domain.enumeration.MakingStatus;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.port.MakingStatusPublisher;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderStatusService {

    private final OrderRepository orderRepository;
    private final MakingStatusPublisher makingStatusPublisher;

    public void markPaid(Order order, LocalDateTime paidAt, String tradeNo, String pickupCode) {
        MakingStatus fromMakingStatus = order.getMakingStatus();
        order.markPaid(paidAt, pickupCode);
        try {
            orderRepository.updatePaymentStatus(order);
            log.info("订单已标记为已支付, orderNo={}, paidAt={}, pickupCode={}, tradeNo={}",
                    order.getOrderNo(), paidAt, pickupCode, order.getTradeNo());
        } catch (RuntimeException e) {
            // 与支付回调/对账并发更新导致乐观锁冲突：本地已被对方更新为已支付，结果一致，无需重试
            log.warn("回写支付状态冲突, 视为已被并发更新, orderNo={}", order.getOrderNo());
            return;
        }
        orderRepository.updateMakingStatus(order);
        if (fromMakingStatus == MakingStatus.PENDING) {
            makingStatusPublisher.publishMakingStatusChange(order.getId(), order.getStoreId(), fromMakingStatus, MakingStatus.PREPARING);
        }
        log.info("订单进入制作中, orderNo={}, fromMakingStatus={}", order.getOrderNo(), fromMakingStatus);
    }

    public void markTimeout(Order order) {
        order.markPaymentTimeout();
        boolean updated = orderRepository.timeoutOrder(order);
        if (!updated) {
            log.info("订单支付状态已被并发变更, 放弃标记支付超时, orderNo={}", order.getOrderNo());
            return;
        }
        log.info("订单已标记为支付超时, orderNo={}, paymentExpiredAt={}", order.getOrderNo(), order.getPaymentExpiredAt());
    }

    public void markReady(Order order) {
        MakingStatus fromMakingStatus = order.getMakingStatus();
        order.markReady();
        orderRepository.updateMakingStatus(order);
        if (fromMakingStatus == MakingStatus.PREPARING) {
            makingStatusPublisher.publishMakingStatusChange(order.getId(), order.getStoreId(), fromMakingStatus, MakingStatus.READY);
        }
        log.info("订单制作完成, orderNo={}, fromMakingStatus={}", order.getOrderNo(), fromMakingStatus);
    }

    public void markCollected(Order order) {
        order.markCollected();
        orderRepository.updateMakingStatus(order);
        log.info("订单已取餐, orderNo={}", order.getOrderNo());
    }
}
