package cn.dextea.trade.order.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public abstract class AbstractOrderResponse {

    @Schema(description = "不可用的商品与客制化")
    private CreateOrderUnavailable unavailable;

    @Schema(description = "剔除不可用项后的有效商品列表")
    private List<CreateOrderProductItem> products;

    @JsonProperty("StoreAvailable")
    @Schema(description = "门店是否可用：门店 ID 合法且处于营业中", example = "true")
    private Boolean storeAvailable;

    @JsonProperty("CustomerAvailable")
    @Schema(description = "顾客是否可用：顾客 ID 合法且处于激活", example = "true")
    private Boolean customerAvailable;

    @Schema(description = "订单商品总数量", example = "2")
    private Integer totalQuantity;

    @Schema(description = "订单总价（元）", example = "25.00")
    private BigDecimal totalPrice;
}
