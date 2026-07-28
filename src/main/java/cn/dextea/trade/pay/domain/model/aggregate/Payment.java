package cn.dextea.trade.pay.domain.model.aggregate;
import cn.dextea.trade.pay.domain.enums.PlatformEnum;
import lombok.Builder;
import lombok.Value;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Value
@Builder
public class Payment {
    PlatformEnum platform;
    String orderNo;
    String customerOpenId;
    Integer totalQuantity;
    BigDecimal totalPrice;
    LocalDateTime payExpireAt;
}
