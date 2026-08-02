package cn.dextea.trade.order.interfaces.http.dto.response;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "订单商品明细")
public class OrderDetailItem {
    @Schema(description = "商品ID", example = "1")
    private Long productId;
    @Schema(description = "商品名称", example = "招牌奶茶")
    private String productName;
    @Schema(description = "商品 SKU 编码", example = "1#1_1-2_6_3_7")
    private String skuId;
    @Schema(description = "商品封面图 URL", example = "https://example.com/example.jpg")
    private String coverUrl;
    @Schema(description = "客制化选项文本，由选项名称拼接而成", example = "少冰 / 少甜 / 茉莉花茶")
    private String customizationText;
    @Schema(description = "购买数量", example = "2")
    private Integer quantity;
    @Schema(description = "商品单价，含客制化加价", example = "12.50")
    private BigDecimal unitPrice;
    @Schema(description = "商品小计，单价 × 数量", example = "25.00")
    private BigDecimal subtotal;
}
