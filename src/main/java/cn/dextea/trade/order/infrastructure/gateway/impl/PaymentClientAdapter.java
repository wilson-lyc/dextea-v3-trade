package cn.dextea.trade.order.infrastructure.gateway.impl;

import cn.dextea.trade.order.domain.gateway.PaymentClientGateway;
import cn.dextea.trade.pay.application.command.CreatePaymentCommand;
import cn.dextea.trade.pay.application.service.PaymentService;
import cn.dextea.trade.pay.domain.enums.PlatformEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付客户端适配器：实现订单领域 {@link PaymentClientGateway}，委派 pay 应用服务创建支付交易。
 */
@Component
@RequiredArgsConstructor
public class PaymentClientAdapter implements PaymentClientGateway {

    private final PaymentService paymentService;

    @Override
    public String createPayment(String orderNo, BigDecimal totalPrice,
                               String customerOpenId, Integer totalQuantity, int platform,
                               LocalDateTime payExpireAt) {
        CreatePaymentCommand command = CreatePaymentCommand.builder()
                .orderNo(orderNo)
                .totalPrice(totalPrice)
                .customerOpenId(customerOpenId)
                .totalQuantity(totalQuantity)
                .platform(PlatformEnum.of(platform))
                .payExpireAt(payExpireAt)
                .build();
        return paymentService.createPayment(command);
    }
}
