package cn.dextea.trade.order.domain.gateway;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付客户端网关：由基础设施层适配 pay 应用服务，降低跨域耦合。
 */
public interface PaymentClientGateway {

    /**
     * 创建支付交易。
     *
     * @param payExpireAt 订单支付过期时间点（由订单系统计算），透传给支付渠道以保证两端关单时刻一致
     */
    String createPayment(String orderNo, BigDecimal totalPrice,
                         String customerOpenId, Integer totalQuantity, int platform,
                         LocalDateTime payExpireAt);
}
