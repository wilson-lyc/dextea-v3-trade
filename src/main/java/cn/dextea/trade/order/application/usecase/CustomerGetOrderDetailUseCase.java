package cn.dextea.trade.order.application.usecase;

import cn.dextea.trade.order.application.assembler.CustomerOrderDetailAssembler;
import cn.dextea.trade.order.application.dto.command.CustomerGetOrderDetailCommand;
import cn.dextea.trade.order.application.dto.result.CustomerOrderDetailResult;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.shared.util.EnsureUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerGetOrderDetailUseCase {

    private final OrderRepository orderRepository;

    public CustomerOrderDetailResult execute(CustomerGetOrderDetailCommand command) {
        Order order = EnsureUtil.notNull(
                orderRepository.getOrderById(command.getOrderId()), OrderErrorCode.ORDER_NOT_FOUND);
        order.ensureBelongsTo(command.getCustomerId());
        return CustomerOrderDetailAssembler.toResult(order);
    }
}
