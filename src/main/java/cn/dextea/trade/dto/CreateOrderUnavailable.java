package cn.dextea.trade.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "不可用项（下架/缺货的商品与定制项）")
public class CreateOrderUnavailable {

    @Schema(description = "不可用的商品列表")
    private List<CreateOrderUnavailableProduct> products;

    @Schema(description = "不可用的定制项列表")
    private List<CreateOrderUnavailableOption> customizationOptions;
}
