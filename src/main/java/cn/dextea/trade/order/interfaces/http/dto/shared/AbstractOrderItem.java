package cn.dextea.trade.order.interfaces.http.dto.shared;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单项")
public class AbstractOrderItem {
    @NotBlank(message = "skuId 不能为空")
    @Schema(description = "商品 SKU 编码", example = "1#1_1-2_6-3_7")
    private String skuId;

    @NotNull(message = "quantity 不能为空")
    @Min(value = 1, message = "quantity 必须大于 0")
    @Schema(description = "购买数量", example = "1")
    private Integer quantity;

    @Schema(description = "商品 ID（响应填充，请求无需传）", example = "1")
    private Long productId;

    @Schema(description = "商品名称（响应填充，请求无需传）", example = "招牌奶茶")
    private String productName;

    @Schema(description = "商品封面图 URL（响应填充，请求无需传）", example = "https://example.com/example.jpg")
    private String cover;

    @Schema(description = "商品客制化（响应填充，请求无需传）", example = "少冰 / 少甜 / 茉莉花茶")
    private String customization;

    @Schema(description = "单价（响应填充，请求无需传）", example = "12.50")
    private BigDecimal unitPrice;

    @Schema(description = "小计（响应填充，请求无需传）", example = "25.00")
    private BigDecimal totalPrice;
}
