package cn.dextea.trade.order.application.usecase;

import cn.dextea.trade.order.application.assembler.OrderDetailAssembler;
import cn.dextea.trade.order.application.dto.command.GetOrderDetailCommand;
import cn.dextea.trade.order.application.dto.result.OrderDetailResult;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.shared.domain.error.BizError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetOrderDetailUseCase {

    private final OrderRepository orderRepository;

    public OrderDetailResult execute(GetOrderDetailCommand command) {
        log.info("查询订单详情, customerId={}, orderId={}", command.getCustomerId(), command.getOrderId());

        Order order = orderRepository.getById(command.getOrderId());
        if (order == null) {
            throw new BizError(OrderErrorCode.ORDER_NOT_FOUND);
        }

        order.ensureBelongsTo(command.getCustomerId());

        OrderDetailResult result = OrderDetailAssembler.toResult(order);
        log.info("查询订单详情成功, customerId={}, orderId={}, itemCount={}",
                command.getCustomerId(), command.getOrderId(), result.getItems() == null ? 0 : result.getItems().size());
        return result;
    }
}
