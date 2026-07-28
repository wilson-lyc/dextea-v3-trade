package cn.dextea.trade.pay.domain.gateway;
import cn.dextea.trade.pay.domain.model.aggregate.Payment;
public interface PaymentGateway {
    String createPayment(Payment payment);
}
