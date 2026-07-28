package cn.dextea.trade.order.api.dto.request;
import cn.dextea.trade.pay.domain.enums.PlatformEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.util.List;
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class AbstractOrderRequest {
    @NotNull(message = "storeId 不能为空")
    @Schema(description = "门店 ID", example = "1")
    private Long storeId;
    @NotNull(message = "platform 不能为空")
    @Schema(description = "仅支持 weixin 和 alipay", example = "alipay")
    private PlatformEnum platform;
    @NotNull(message = "diningMethod 不能为空")
    @Schema(description = "用餐方式：0 堂食，1 外带", example = "0")
    private Integer diningMethod;
    @Size(max = 500, message = "备注不能超过 500 字")
    @Schema(description = "订单备注（选填）", example = "少放辣椒，不要香菜")
    private String note;
    @Valid
    @NotEmpty(message = "products 不能为空")
    @Schema(description = "商品明细列表")
    private List<CreateOrderProductItem> products;
}
