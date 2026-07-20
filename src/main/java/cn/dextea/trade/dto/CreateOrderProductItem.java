package cn.dextea.trade.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "下单商品明细项")
public class CreateOrderProductItem {

    @NotBlank(message = "skuId 不能为空")
    @Schema(description = "商品 SKU 编码", example = "1001-2001-0")
    private String skuId;

    @NotNull(message = "quantity 不能为空")
    @Schema(description = "购买数量", example = "2")
    private Integer quantity;
}
