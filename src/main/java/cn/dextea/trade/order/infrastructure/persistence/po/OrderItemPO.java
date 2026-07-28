package cn.dextea.trade.order.infrastructure.persistence.po;
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
public class OrderItemPO {
    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    private String skuId;
    private Long coverId;
    private String customizationText;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
