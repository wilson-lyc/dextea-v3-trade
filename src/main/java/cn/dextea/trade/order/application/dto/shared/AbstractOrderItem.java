package cn.dextea.trade.order.application.dto.shared;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AbstractOrderItem {
    private String skuId;
    private Integer quantity;
    private Long productId;
    private String productName;
    private String cover;
    private String customization;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}
