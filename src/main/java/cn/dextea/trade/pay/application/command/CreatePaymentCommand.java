package cn.dextea.trade.pay.application.command;
import cn.dextea.trade.pay.domain.enums.PlatformEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentCommand {
    private String orderNo;
    private BigDecimal totalPrice;
    private String customerOpenId;
    private Integer totalQuantity;
    private PlatformEnum platform;
    private LocalDateTime payExpireAt;
}
