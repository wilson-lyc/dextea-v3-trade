package cn.dextea.trade.order.api.dto.response;

import cn.dextea.trade.order.api.dto.request.CreateOrderProductItem;
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
public abstract class AbstractCreateOrderResponse {
    @Schema(description = "不可用项")
    private List<CreateOrderItem> unavailable;

    @Schema(description = "可用项")
    private List<CreateOrderItem> available;

    @Schema(description = "订单商品总数量", example = "2")
    private Integer totalQuantity;

    @Schema(description = "订单总价（元）", example = "25.00")
    private BigDecimal totalPrice;
}
