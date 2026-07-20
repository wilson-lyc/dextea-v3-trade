package cn.dextea.trade.dto;

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
public class CreateOrderProductItem {

    @NotBlank(message = "skuId 不能为空")
    private String skuId;

    @NotNull(message = "quantity 不能为空")
    private Integer quantity;
}
