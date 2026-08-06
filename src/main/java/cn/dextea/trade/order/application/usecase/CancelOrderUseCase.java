package cn.dextea.trade.order.application.usecase;

import cn.dextea.trade.order.application.dto.command.CancelOrderCommand;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.shared.domain.error.BizError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CancelOrderUseCase {

    private final OrderRepository orderRepository;

    public void execute(CancelOrderCommand command) {
        log.info("取消订单请求, customerId={}, orderId={}", command.getCustomerId(), command.getOrderId());

        Order order = orderRepository.getOrderById(command.getOrderId());
        if (order == null) {
            throw new BizError(OrderErrorCode.ORDER_NOT_FOUND);
        }

        order.ensureBelongsTo(command.getCustomerId());

        order.ensurePendingPayment();

        order.markCancelled();
        orderRepository.cancelOrder(order);
        log.info("取消订单成功, customerId={}, orderId={}", command.getCustomerId(), command.getOrderId());
    }
}
