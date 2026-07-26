package cn.dextea.trade.pay.domain.port;

import cn.dextea.trade.pay.domain.model.Payment;

/**
 * 支付端口
 */
public interface PaymentPort {

    /**
     * 创建交易
     *
     * @param payment 支付领域对象
     * @return 交易号
     */
    String createPayment(Payment payment);
}
