package cn.dextea.trade.pay.domain.model.aggregate;
import cn.dextea.trade.shared.domain.enumeration.PaymentMethod;
import lombok.Builder;
import lombok.Value;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Value
@Builder
public class Payment {
    PaymentMethod platform;
    String orderNo;
    String customerOpenId;
    Integer totalQuantity;
    BigDecimal totalPrice;
    LocalDateTime payExpireAt;
}
