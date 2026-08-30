package cn.dextea.trade.order.interfaces.http.dto.shared;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AbstractOrderItem {
    @NotBlank(message = "skuId 不能为空")
    private String skuId;

    @NotNull(message = "quantity 不能为空")
    @Min(value = 1, message = "quantity 必须大于 0")
    private Integer quantity;

    private String product;

    private String customization;

    private String cover;

    private BigDecimal unitPrice;

    private BigDecimal totalPrice;
}
