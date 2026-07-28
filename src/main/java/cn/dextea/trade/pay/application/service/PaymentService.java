package cn.dextea.trade.pay.application.service;
import cn.dextea.trade.pay.application.command.CreatePaymentCommand;
public interface PaymentService {
    String createPayment(CreatePaymentCommand command);
}
