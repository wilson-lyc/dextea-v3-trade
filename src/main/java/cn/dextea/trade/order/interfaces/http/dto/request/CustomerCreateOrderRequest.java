package cn.dextea.trade.order.interfaces.http.dto.request;
import cn.dextea.trade.order.interfaces.http.dto.shared.CustomerCreateOrderItem;
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
public class CustomerCreateOrderRequest extends AbstractCustomerCreateOrderRequest<CustomerCreateOrderItem> {
    @NotBlank(message = "idempotencyKey 不能为空")
    @Size(max = 64, message = "idempotencyKey 长度不能超过 64")
    private String idempotencyKey;

    @NotNull(message = "diningMethod 不能为空")
    private Integer diningMethod;

    @NotNull(message = "source 不能为空")
    private Integer source;

    @NotNull(message = "paymentMethod 不能为空")
    private Integer paymentMethod;

    @Size(max = 500, message = "备注不能超过 500 字")
    private String note;
}
