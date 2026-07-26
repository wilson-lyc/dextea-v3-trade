package cn.dextea.trade.order.interfaces.dto;

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
@Schema(description = "不可用的商品和客制化列表")
public class CreateOrderUnavailable {

    @Schema(description = "不可用的商品列表")
    private List<CreateOrderUnavailableProduct> products;

    @Schema(description = "不可用的客制化列表")
    private List<CreateOrderUnavailableCustomization> customization;
}
