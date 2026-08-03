package cn.dextea.trade.order.interfaces.http.dto.request;
import cn.dextea.trade.order.interfaces.http.dto.shared.CreateOrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "创建订单请求")
public class CreateOrderRequest extends AbstractCreateOrderRequest<CreateOrderItem> {
    @NotBlank(message = "idempotencyKey 不能为空")
    @Schema(description = "由客户端生成", example = "f47ac10b58cc4372a5670e02b2c3d479")
    private String idempotencyKey;

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
}
