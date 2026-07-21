package cn.dextea.trade.dto;

import cn.dextea.trade.entity.enums.Platform;
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
    @Schema(description = "门店 ID", example = "1")
    private Long storeId;

    @NotNull(message = "customerId 不能为空")
    @Schema(description = "用户 ID", example = "1")
    private Long customerId;

    @NotNull(message = "platform 不能为空")
    @Schema(description = "仅支持 weixin 和 alipay", example = "alipay")
    private Platform platform;

    @NotBlank(message = "diningMethod 不能为空")
    @Schema(description = "仅支持 dine_in 和 takeout", example = "dine_in")
    private String diningMethod;

    @NotBlank(message = "idempotencyKey 不能为空")
    @Schema(description = "由客户端生成", example = "f47ac10b58cc4372a5670e02b2c3d479")
    private String idempotencyKey;

    @Valid
    @NotEmpty(message = "products 不能为空")
    @Schema(description = "商品明细列表")
    private List<CreateOrderProductItem> products;
}
