package cn.dextea.trade.order.application.usecase;

import cn.dextea.trade.order.application.assembler.OrderPaymentStatusAssembler;
import cn.dextea.trade.order.application.dto.command.GetOrderPaymentStatusCommand;
import cn.dextea.trade.order.application.dto.result.OrderPaymentStatusResult;
import cn.dextea.trade.order.application.service.PaymentReconciliationService;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.shared.error.BizError;
import cn.dextea.trade.shared.util.EnsureUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetOrderPaymentStatusUseCase {

    private final OrderRepository orderRepository;
    private final PaymentReconciliationService paymentReconciliationService;

    public OrderPaymentStatusResult execute(GetOrderPaymentStatusCommand command) {
        Long customerId = command.getCustomerId();
        Long orderId = command.getOrderId();
        log.info("查询订单支付状态, customerId={}, orderId={}", customerId, orderId);

        Order order = EnsureUtil.notNull(orderRepository.getSummaryById(orderId), OrderErrorCode.ORDER_NOT_FOUND);

        order.ensureBelongsTo(customerId);

        paymentReconciliationService.reconcileIfPending(order);

        log.info("查询订单支付状态成功, customerId={}, orderId={}, paymentStatus={}",
                customerId, orderId, order.getPaymentStatus());
        return OrderPaymentStatusAssembler.toResult(order);
    }
}
