package cn.dextea.trade.dto;

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
public class CreateOrderRequest {

    @NotNull(message = "storeId 不能为空")
    private Long storeId;

    @NotBlank(message = "diningMethod 不能为空")
    private String diningMethod;

    @NotEmpty(message = "products 不能为空")
    @Valid
    private List<CreateOrderProductItem> products;
}
