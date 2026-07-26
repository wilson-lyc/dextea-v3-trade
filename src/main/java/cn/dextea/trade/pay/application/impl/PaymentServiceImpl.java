package cn.dextea.trade.pay.application.impl;

import cn.dextea.trade.pay.application.PaymentService;
import cn.dextea.trade.pay.application.command.CreatePaymentCommand;
import cn.dextea.trade.pay.domain.gateway.PaymentGateway;
import cn.dextea.trade.pay.domain.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 支付服务实现：将创建支付命令转换为支付领域对象，委派给 {@link PaymentGateway}。
 */
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentGateway paymentGateway;

    @Override
    public String createPayment(CreatePaymentCommand command) {
        Payment payment = Payment.builder()
                .orderNo(command.getOrderNo())
                .totalPrice(command.getTotalPrice())
                .customerOpenId(command.getCustomerOpenId())
                .totalQuantity(command.getTotalQuantity())
                .platform(command.getPlatform())
                .build();
        return paymentGateway.createPayment(payment);
    }
}
