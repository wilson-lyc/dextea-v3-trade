package cn.dextea.trade.pay.application.service;

import cn.dextea.trade.pay.application.command.CreatePaymentCommand;

/**
 * 支付服务
 */
public interface PaymentService {

    /**
     * 创建一笔支付交易。
     *
     * @param command 创建支付命令
     * @return 交易号
     */
    String createPayment(CreatePaymentCommand command);
}
