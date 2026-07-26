package cn.dextea.trade.pay.domain.gateway;

import cn.dextea.trade.pay.domain.model.Payment;

/**
 * 支付网关防腐层接口
 */
public interface PaymentGateway {

    /**
     * 创建一笔交易。
     *
     * @param payment 支付领域对象
     * @return 支付渠道交易号
     */
    String createPayment(Payment payment);
}
