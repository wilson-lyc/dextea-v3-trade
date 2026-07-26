package cn.dextea.trade.order.infrastructure.adapter;

import cn.dextea.trade.order.domain.port.PaymentClientPort;
import cn.dextea.trade.pay.application.PaymentService;
import cn.dextea.trade.pay.application.command.CreatePaymentCommand;
import cn.dextea.trade.pay.domain.model.PlatformEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 支付客户端适配器：实现订单领域 {@link PaymentClientPort}，委派 pay 应用服务创建支付交易。
 */
@Component
@RequiredArgsConstructor
public class PaymentClientAdapter implements PaymentClientPort {

    private final PaymentService paymentService;

    @Override
    public String createPayment(String orderNo, BigDecimal totalPrice,
                               String customerOpenId, Integer totalQuantity, int platform) {
        CreatePaymentCommand command = CreatePaymentCommand.builder()
                .orderNo(orderNo)
                .totalPrice(totalPrice)
                .customerOpenId(customerOpenId)
                .totalQuantity(totalQuantity)
                .platform(PlatformEnum.of(platform))
                .build();
        return paymentService.createPayment(command);
    }
}
