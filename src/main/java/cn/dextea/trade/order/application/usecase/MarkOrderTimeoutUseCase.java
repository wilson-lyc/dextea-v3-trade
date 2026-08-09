package cn.dextea.trade.order.application.usecase;

import cn.dextea.trade.order.application.dto.command.MarkOrderTimeoutCommand;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.order.domain.service.OrderStatusService;
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
    private final OrderStatusService orderStatusService;

    @Transactional
    public void execute(MarkOrderTimeoutCommand command) {
        String orderNo = command.getOrderNo();
        Order order = orderRepository.getSummaryByOrderNo(orderNo);
        if (order == null) {
            log.error("订单支付超时处理失败, 订单不存在, orderNo={}", orderNo);
            throw new BizError(OrderErrorCode.ORDER_NOT_FOUND);
        }

        orderStatusService.markTimeout(order);
    }
}
