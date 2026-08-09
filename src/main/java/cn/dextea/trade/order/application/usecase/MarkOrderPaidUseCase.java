package cn.dextea.trade.order.application.usecase;

import cn.dextea.trade.order.application.dto.command.MarkOrderPaidCommand;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.exception.RetryableOrderException;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.order.domain.service.OrderPaymentService;
import cn.dextea.trade.order.domain.service.PickupCodeGenerator;
import cn.dextea.trade.shared.error.BizError;
import cn.dextea.trade.shared.model.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarkOrderPaidUseCase {

    private final OrderRepository orderRepository;
    private final OrderPaymentService orderPaymentService;
    private final PickupCodeGenerator pickupCodeGenerator;

    @Transactional
    public void execute(MarkOrderPaidCommand command) {
        String orderNo = command.getOrderNo();
        String tradeNo = command.getTradeNo();
        Order order = orderRepository.getSummaryByOrderNo(orderNo);
        if (order == null) {
            throw new RetryableOrderException(
                    "订单已支付事件处理时订单暂未查到, 等待重投, orderNo=" + orderNo + ", tradeNo=" + tradeNo);
        }

        verifyAmount(order, command.getPaidAmount(), orderNo, tradeNo);

        String pickupCode = pickupCodeGenerator.generate(order.getStoreId(), LocalDate.now());
        orderPaymentService.markPaid(order, command.getPaidAt(), tradeNo, pickupCode);
    }

    private void verifyAmount(Order order, BigDecimal paidAmount, String orderNo, String tradeNo) {
        if (paidAmount == null) {
            return;
        }
        Money orderAmount = order.getTotalPrice();
        Money callbackAmount;
        try {
            callbackAmount = Money.of(paidAmount);
        } catch (IllegalArgumentException e) {
            log.error("支付回调金额非法, orderNo={}, tradeNo={}, paidAmount={}", orderNo, tradeNo, paidAmount);
            throw new BizError(OrderErrorCode.ORDER_PAID_AMOUNT_MISMATCH, "支付回调金额非法");
        }
        if (orderAmount.isGreaterThan(callbackAmount) || callbackAmount.isGreaterThan(orderAmount)) {
            log.error("支付回调金额与订单金额不一致, orderNo={}, tradeNo={}, orderAmount={}, paidAmount={}",
                    orderNo, tradeNo, orderAmount, callbackAmount);
            throw new BizError(OrderErrorCode.ORDER_PAID_AMOUNT_MISMATCH);
        }
    }
}
