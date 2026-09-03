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
        // 单条 SQL 原子更新支付状态与制作状态；返回 false 表示另一条路线（回调/主动查询）已完成流转，幂等跳过
        if (!orderRepository.markPaid(order)) {
            log.info("订单已被并发标记为已支付, 跳过本次流转, orderNo={}", order.getOrderNo());
            return;
        }
        log.info("订单已标记为已支付, orderNo={}, paidAt={}, pickupCode={}, tradeNo={}",
                order.getOrderNo(), paidAt, order.getPickupCode(), tradeNo);
        if (fromMakingStatus == MakingStatus.PENDING) {
            makingStatusPublisher.publishMakingStatusChange(order, fromMakingStatus, MakingStatus.PREPARING);
        }
        log.info("订单进入制作中, orderNo={}, fromMakingStatus={}", order.getOrderNo(), fromMakingStatus);
    }

    public void markTimeout(Order order) {
        order.markPaymentTimeout();
        boolean updated = orderRepository.timeoutOrder(order);
        if (!updated) {
            log.info("订单支付状态已被并发变更或超时时间未到, 放弃标记支付超时, orderNo={}", order.getOrderNo());
            return;
        }
        log.info("订单已标记为支付超时, orderNo={}, paymentExpiredAt={}", order.getOrderNo(), order.getPaymentExpiredAt());
    }

    public void markReady(Order order) {
        MakingStatus fromMakingStatus = order.getMakingStatus();
        order.markReady();
        orderRepository.updateMakingStatus(order);
        if (fromMakingStatus == MakingStatus.PREPARING) {
            makingStatusPublisher.publishMakingStatusChange(order, fromMakingStatus, MakingStatus.READY);
        }
        log.info("订单制作完成, orderNo={}, fromMakingStatus={}", order.getOrderNo(), fromMakingStatus);
    }

    public void markCollected(Order order) {
        order.markCollected();
        orderRepository.updateMakingStatus(order);
        log.info("订单已取餐, orderNo={}", order.getOrderNo());
    }
}
