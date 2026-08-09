package cn.dextea.trade.order.application.usecase;

import cn.dextea.trade.order.application.dto.command.MarkOrderCollectedCommand;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.order.domain.service.OrderMakingStatusService;
import cn.dextea.trade.shared.error.BizError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarkOrderCollectedUseCase {

    private final OrderRepository orderRepository;
    private final OrderMakingStatusService orderMakingStatusService;

    public void execute(MarkOrderCollectedCommand command) {
        log.info("订单已取餐请求, customerId={}, orderId={}", command.getCustomerId(), command.getOrderId());

        Order order = orderRepository.getOrderById(command.getOrderId());
        if (order == null) {
            throw new BizError(OrderErrorCode.ORDER_NOT_FOUND);
        }

        order.ensureBelongsTo(command.getCustomerId());

        orderMakingStatusService.markCollected(order);
        log.info("订单已取餐成功, customerId={}, orderId={}", command.getCustomerId(), command.getOrderId());
    }
}
