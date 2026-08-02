package cn.dextea.trade.order.interfaces.http.dto.response;

import cn.dextea.trade.order.interfaces.http.dto.shared.AbstractOrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class AbstractCreateOrderResponse<T extends AbstractOrderItem> {
    @Schema(description = "不可用项")
    private List<T> unavailable;

    @Schema(description = "可用项")
    private List<T> available;

    @Schema(description = "订单商品总数量", example = "2")
    private Integer totalQuantity;

    @Schema(description = "订单总价（元）", example = "25.00")
    private BigDecimal totalPrice;
}
