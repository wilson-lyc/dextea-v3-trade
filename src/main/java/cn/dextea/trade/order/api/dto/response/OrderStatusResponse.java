package cn.dextea.trade.order.api.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusResponse {
    private Long orderId;
    private String orderNo;
    private String tradeNo;
    private Integer tradeStatus;
    private Integer makingStatus;
    private String pickupCode;
    private LocalDateTime payExpireAt;
    private LocalDateTime paidAt;
    private LocalDateTime updatedAt;
    private Boolean terminal;
}
