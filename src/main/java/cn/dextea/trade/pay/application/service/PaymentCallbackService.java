package cn.dextea.trade.pay.application.service;

import cn.dextea.trade.pay.api.dto.PaymentCallbackMessage;

/**
 * 支付回单处理应用服务。
 *
 * <p>负责把 RocketMQ 中收到的支付平台回单转换为订单状态变更。</p>
 */
public interface PaymentCallbackService {

    /**
     * 处理一条支付回单消息。
     *
     * @param message 支付平台回单消息
     */
    void handleCallback(PaymentCallbackMessage message);
}
