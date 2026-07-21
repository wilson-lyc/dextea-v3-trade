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
@Schema(description = "不可用的商品")
public class CreateOrderUnavailableProduct {

    @Schema(description = "商品 ID", example = "1")
    private Long id;

    @Schema(description = "商品名称", example = "招牌奶茶")
    private String name;
}
