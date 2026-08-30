package cn.dextea.trade.order.interfaces.http.dto.request;
import cn.dextea.trade.order.interfaces.http.dto.shared.AbstractOrderItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class AbstractCustomerCreateOrderRequest<T extends AbstractOrderItem> {
    @NotNull(message = "storeId 不能为空")
    private Long storeId;

    @Valid
    @NotEmpty(message = "items 不能为空")
    private List<T> items;
}
