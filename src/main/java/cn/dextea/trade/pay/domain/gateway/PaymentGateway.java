package cn.dextea.trade.pay.domain.gateway;

import cn.dextea.trade.pay.domain.model.Payment;

/**
 * 支付网关
 */
public interface PaymentGateway {

    /**
     * 创建交易
     *
     * @param payment 支付领域对象
     * @return 交易号
     */
    String createPayment(Payment payment);
}
