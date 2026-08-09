package cn.dextea.trade.order.application.usecase;

import cn.dextea.trade.order.application.dto.command.MarkOrderReadyCommand;
import cn.dextea.trade.order.domain.enumeration.MakingStatus;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.port.MakingStatusPublisher;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.shared.error.BizError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarkOrderReadyUseCase {

    private final OrderRepository orderRepository;
    private final MakingStatusPublisher makingStatusPublisher;

    public void execute(MarkOrderReadyCommand command) {
        log.info("订单制作完成请求, customerId={}, orderId={}", command.getCustomerId(), command.getOrderId());

        Order order = orderRepository.getOrderById(command.getOrderId());
        if (order == null) {
            throw new BizError(OrderErrorCode.ORDER_NOT_FOUND);
        }

        order.ensureBelongsTo(command.getCustomerId());

        MakingStatus fromStatus = order.getMakingStatus();
        order.markReady();
        orderRepository.updateMakingStatus(order);
        if (fromStatus == MakingStatus.PREPARING) {
            makingStatusPublisher.publishMakingStatusChange(order.getOrderNo(), fromStatus, MakingStatus.READY);
        }
        log.info("订单制作完成成功, customerId={}, orderId={}", command.getCustomerId(), command.getOrderId());
    }
}
