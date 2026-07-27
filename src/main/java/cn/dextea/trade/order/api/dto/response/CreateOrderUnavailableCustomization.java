package cn.dextea.trade.order.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "不可用的客制化项")
public class CreateOrderUnavailableCustomization {

    @Schema(description = "客制化选项 ID", example = "1")
    private Long optionId;

    @Schema(description = "客制化选项名称", example = "少糖")
    private String optionName;

    @Schema(description = "商品 ID", example = "1")
    private Long productId;

    @Schema(description = "商品名称", example = "招牌奶茶")
    private String productName;

    @Schema(description = "客制化项目 ID", example = "1")
    private Long itemId;

    @Schema(description = "客制化项目名称", example = "甜度")
    private String itemName;
}
