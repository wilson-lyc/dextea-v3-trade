package cn.dextea.trade.pay.application.service.impl;

import cn.dextea.trade.pay.application.command.CreatePaymentCommand;
import cn.dextea.trade.pay.application.service.PaymentService;
import cn.dextea.trade.pay.domain.gateway.PaymentGateway;
import cn.dextea.trade.pay.domain.model.aggregate.Payment;
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
                .payExpireAt(command.getPayExpireAt())
                .build();
        return paymentGateway.createPayment(payment);
    }
}
