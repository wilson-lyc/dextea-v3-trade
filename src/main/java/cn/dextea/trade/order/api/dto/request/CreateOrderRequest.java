package cn.dextea.trade.order.api.dto.request;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
public class CreateOrderRequest extends AbstractCreateOrderRequest {
    @NotBlank(message = "idempotencyKey 不能为空")
    @Schema(description = "由客户端生成", example = "f47ac10b58cc4372a5670e02b2c3d479")
    private String idempotencyKey;
}
