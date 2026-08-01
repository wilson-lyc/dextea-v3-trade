package cn.dextea.trade.order.domain.port;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PaymentClient {

    String createPayment(String orderNo, BigDecimal totalPrice,
                         String customerOpenId, Integer totalQuantity, int platform,
                         LocalDateTime payExpireAt);
}
