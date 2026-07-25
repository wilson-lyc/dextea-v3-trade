package cn.dextea.trade.service;

import cn.dextea.trade.model.PaymentNotifyMessage;

/**
 * 支付回单消息处理服务。
 *
 * <p>负责把 RocketMQ 中收到的支付平台回单转换为订单状态变更。</p>
 */
public interface PaymentNotifyService {

    /**
     * 处理一条支付回单消息。
     *
     * @param message 支付平台回单消息
     */
    void handleNotify(PaymentNotifyMessage message);
}
