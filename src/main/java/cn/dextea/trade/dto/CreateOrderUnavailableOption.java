package cn.dextea.trade.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "不可用的定制项")
public class CreateOrderUnavailableOption {

    @Schema(description = "定制项 ID", example = "3001")
    private Long optionId;

    @Schema(description = "定制项名称", example = "少糖")
    private String optionName;

    @Schema(description = "所属商品 ID", example = "2001")
    private Long productId;

    @Schema(description = "所属商品名称", example = "招牌奶茶")
    private String productName;
}
