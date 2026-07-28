package cn.dextea.trade.order.application.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryDTO {
    private Long orderId;
    private String storeName;
    private LocalDateTime orderTime;
    private Integer tradeStatus;
    private Integer makingStatus;
    private BigDecimal totalPrice;
    private Integer totalQuantity;
    private List<String> coverUrls;
}
