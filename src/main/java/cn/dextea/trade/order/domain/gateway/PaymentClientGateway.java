package cn.dextea.trade.order.domain.gateway;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public interface PaymentClientGateway {
    String createPayment(String orderNo, BigDecimal totalPrice,
                         String customerOpenId, Integer totalQuantity, int platform,
                         LocalDateTime payExpireAt);
}
