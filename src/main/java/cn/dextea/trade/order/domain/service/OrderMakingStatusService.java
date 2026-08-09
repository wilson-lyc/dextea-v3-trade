package cn.dextea.trade.order.domain.service;

import cn.dextea.trade.order.domain.enumeration.MakingStatus;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.port.MakingStatusPublisher;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderMakingStatusService {

    private final OrderRepository orderRepository;
    private final MakingStatusPublisher makingStatusPublisher;

    public void startMaking(Order order, MakingStatus fromMakingStatus) {
        orderRepository.updateMakingStatus(order);
        if (fromMakingStatus == MakingStatus.PENDING) {
            makingStatusPublisher.publishMakingStatusChange(order.getOrderNo(), fromMakingStatus, MakingStatus.PREPARING);
        }
        log.info("订单进入制作中, orderNo={}, fromMakingStatus={}", order.getOrderNo(), fromMakingStatus);
    }

    public void markReady(Order order) {
        MakingStatus fromMakingStatus = order.getMakingStatus();
        order.markReady();
        orderRepository.updateMakingStatus(order);
        if (fromMakingStatus == MakingStatus.PREPARING) {
            makingStatusPublisher.publishMakingStatusChange(order.getOrderNo(), fromMakingStatus, MakingStatus.READY);
        }
        log.info("订单制作完成, orderNo={}, fromMakingStatus={}", order.getOrderNo(), fromMakingStatus);
    }

    public void markCollected(Order order) {
        order.markCollected();
        orderRepository.updateMakingStatus(order);
        log.info("订单已取餐, orderNo={}", order.getOrderNo());
    }
}
