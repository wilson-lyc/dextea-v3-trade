package cn.dextea.trade.order.domain.model.valueobject;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import java.math.BigDecimal;
@Getter
@Builder
@Jacksonized
public class PricedOrderItem {
    private String skuId;
    private Integer quantity;
    private Long productId;
    private String productName;
    private Long coverId;
    private String coverUrl;
    private String customizationText;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
