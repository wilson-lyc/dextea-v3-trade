package cn.dextea.trade.pay.application.service;
import cn.dextea.trade.pay.interface_.mq.dto.PaymentCallbackMessage;
public interface PaymentCallbackService {
    void handleCallback(PaymentCallbackMessage message);
}
