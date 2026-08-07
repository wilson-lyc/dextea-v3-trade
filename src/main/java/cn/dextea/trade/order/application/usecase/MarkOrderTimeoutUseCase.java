package cn.dextea.trade.order.application.usecase;

import cn.dextea.trade.order.application.dto.command.MarkOrderTimeoutCommand;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.shared.error.BizError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarkOrderTimeoutUseCase {

    private final OrderRepository orderRepository;

    @Transactional
    public void execute(MarkOrderTimeoutCommand command) {
        String orderNo = command.getOrderNo();
        Order order = orderRepository.getSummaryByOrderNo(orderNo);
        if (order == null) {
            log.error("订单支付超时处理失败, 订单不存在, orderNo={}", orderNo);
            throw new BizError(OrderErrorCode.ORDER_NOT_FOUND);
        }

        // 仅未支付订单可置为支付超时，其余状态（已支付/已取消/退款中/已退款/已超时）均直接跳过
        if (!order.canMarkPaymentTimeout()) {
            log.info("订单当前状态不允许标记为支付超时, 忽略本次超时消息, orderNo={}, paymentStatus={}",
                    orderNo, order.getPaymentStatus());
            return;
        }

        order.markPaymentTimeout();
        boolean updated = orderRepository.timeoutOrder(order);
        if (!updated) {
            log.info("订单支付状态已被并发变更, 放弃标记支付超时, orderNo={}", orderNo);
            return;
        }
        log.info("订单已标记为支付超时, orderNo={}, paymentExpiredAt={}", orderNo, order.getPaymentExpiredAt());
    }
}
