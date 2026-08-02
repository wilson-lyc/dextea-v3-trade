package cn.dextea.trade.order.application.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderItem {
    private String skuId;
    private Integer quantity;
    private Long productId;
    private String productName;
    private String cover;
    private String customization;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}
