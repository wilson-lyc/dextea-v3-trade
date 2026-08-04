package cn.dextea.trade.order.application.usecase;

import cn.dextea.trade.order.application.dto.command.MarkOrderPaidCommand;
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
public class MarkOrderPaidUseCase {

    private final OrderRepository orderRepository;

    public void execute(MarkOrderPaidCommand command) {
        String orderNo = command.getOrderNo();
        String tradeNo = command.getTradeNo();
        Order order = orderRepository.findByOrderNo(orderNo);
        if (order == null) {
            log.error("订单已支付事件处理失败, 订单不存在, orderNo={}, tradeNo={}", orderNo, tradeNo);
            throw new BizError(OrderErrorCode.ORDER_NOT_FOUND);
        }

        if (order.isPaid()) {
            log.info("订单已是已支付状态, 忽略重复支付回调, orderNo={}, tradeNo={}", orderNo, tradeNo);
            return;
        }

        order.markPaid(command.getPaidAt());
        orderRepository.updatePaymentStatus(order);
        log.info("订单已标记为已支付, orderNo={}, tradeNo={}, paidAt={}", orderNo, tradeNo, command.getPaidAt());
    }
}
