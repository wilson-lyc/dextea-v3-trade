package cn.dextea.trade.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建订单商品项")
public class CreateOrderProductItem {

    @NotBlank(message = "skuId 不能为空")
    @Schema(description = "商品 SKU 编码", example = "1#1_1-2_6_3_7")
    private String skuId;

    @NotNull(message = "quantity 不能为空")
    @Min(value = 1, message = "quantity 必须大于 0")
    @Schema(description = "购买数量", example = "2")
    private Integer quantity;

    @Schema(description = "商品 ID（响应填充，请求无需传）", example = "1")
    private Long productId;

    @Schema(description = "商品名称（响应填充，请求无需传）", example = "招牌奶茶")
    private String productName;

    @Schema(description = "商品封面图 ID（响应填充，请求无需传）", example = "1")
    private Long coverId;

    @Schema(description = "商品封面图 URL（响应填充，请求无需传）", example = "https://cdn.example.com/cover/1.jpg")
    private String coverUrl;

    @Schema(description = "客制化选项文本，由选项名称拼接而成（响应填充，请求无需传）", example = "少冰 / 少甜 / 茉莉花茶")
    private String customizationText;

    @Schema(description = "商品单价，含客制化加价（响应填充，请求无需传）", example = "12.50")
    private BigDecimal unitPrice;

    @Schema(description = "商品小计，单价 × 数量（响应填充，请求无需传）", example = "25.00")
    private BigDecimal subtotal;
}
