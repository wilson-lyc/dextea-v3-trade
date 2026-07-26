package cn.dextea.trade.pay.application;

import cn.dextea.trade.pay.application.command.CreatePaymentCommand;

/**
 * 支付应用服务：支付域对外暴露的渠道无关支付能力。
 */
public interface PaymentService {

    /**
     * 创建一笔支付交易。
     *
     * @param command 创建支付命令
     * @return 支付渠道交易号（trade_no）
     */
    String createPayment(CreatePaymentCommand command);
}
