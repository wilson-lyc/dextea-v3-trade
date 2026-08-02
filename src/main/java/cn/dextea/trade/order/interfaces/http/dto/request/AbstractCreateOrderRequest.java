package cn.dextea.trade.order.interfaces.http.dto.request;
import cn.dextea.trade.order.interfaces.http.dto.shared.AbstractOrderItem;
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
public abstract class AbstractCreateOrderRequest<T extends AbstractOrderItem> {
    @NotNull(message = "storeId 不能为空")
    @Schema(description = "门店 ID", example = "1")
    private Long storeId;

    @NotNull(message = "diningMethod 不能为空")
    @Schema(description = "用餐方式", example = "0")
    private Integer diningMethod;

    @NotNull(message = "source 不能为空")
    @Schema(description = "订单来源", example = "0")
    private Integer source;

    @NotNull(message = "paymentMethod 不能为空")
    @Schema(description = "支付方式", example = "0")
    private Integer paymentMethod;

    @Size(max = 500, message = "备注不能超过 500 字")
    @Schema(description = "订单备注（选填）", example = "少放辣椒，不要香菜")
    private String note;

    @Valid
    @NotEmpty(message = "items 不能为空")
    @Schema(description = "订单明细列表")
    private List<T> items;
}
