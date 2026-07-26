package cn.dextea.trade.order.domain.port;

import java.math.BigDecimal;

/**
 * 支付客户端端口：由基础设施层适配 pay 应用服务，降低跨域耦合。
 */
public interface PaymentClientPort {

    String createPayment(String orderNo, BigDecimal totalPrice,
                         String customerOpenId, Integer totalQuantity, int platform);
}
