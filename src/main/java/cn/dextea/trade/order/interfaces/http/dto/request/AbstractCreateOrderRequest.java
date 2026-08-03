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

    @Valid
    @NotEmpty(message = "items 不能为空")
    @Schema(description = "订单明细列表")
    private List<T> items;
}
