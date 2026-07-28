package cn.dextea.trade.pay.application.service;
import cn.dextea.trade.pay.api.dto.PaymentCallbackMessage;
public interface PaymentCallbackService {
    void handleCallback(PaymentCallbackMessage message);
}
