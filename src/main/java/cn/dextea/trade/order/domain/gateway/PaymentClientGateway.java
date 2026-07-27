package cn.dextea.trade.order.domain.gateway;

import java.math.BigDecimal;

/**
 * 支付客户端网关：由基础设施层适配 pay 应用服务，降低跨域耦合。
 */
public interface PaymentClientGateway {

    String createPayment(String orderNo, BigDecimal totalPrice,
                         String customerOpenId, Integer totalQuantity, int platform);
}
