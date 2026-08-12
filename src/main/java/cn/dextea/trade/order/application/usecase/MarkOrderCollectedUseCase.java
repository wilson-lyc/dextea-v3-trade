package cn.dextea.trade.order.application.usecase;

import cn.dextea.trade.order.application.dto.command.MarkOrderCollectedCommand;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.order.domain.service.OrderStatusService;
import cn.dextea.trade.shared.error.BizError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarkOrderCollectedUseCase {

    private final OrderRepository orderRepository;
    private final OrderStatusService orderStatusService;

    public void execute(MarkOrderCollectedCommand command) {
        log.info("订单已取餐请求, storeId={}, orderId={}", command.getStoreId(), command.getOrderId());

        Order order = orderRepository.getOrderById(command.getOrderId());
        if (order == null) {
            throw new BizError(OrderErrorCode.ORDER_NOT_FOUND);
        }

        order.ensureBelongsToStore(command.getStoreId());
        order.ensureCanMarkCollected();

        orderStatusService.markCollected(order);
        log.info("订单已取餐成功, storeId={}, orderId={}", command.getStoreId(), command.getOrderId());
    }
}
