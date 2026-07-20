package cn.dextea.trade.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "下单计价请求")
public class CreateOrderRequest {

    @NotNull(message = "storeId 不能为空")
    @Schema(description = "门店 ID", example = "1001")
    private Long storeId;

    @NotBlank(message = "diningMethod 不能为空")
    @Schema(description = "就餐方式，如 dine_in（堂食）/ takeout（外带）", example = "dine_in")
    private String diningMethod;

    @NotEmpty(message = "products 不能为空")
    @Valid
    @Schema(description = "商品明细列表")
    private List<CreateOrderProductItem> products;
}
