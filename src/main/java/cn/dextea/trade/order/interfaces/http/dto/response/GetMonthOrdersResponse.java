package cn.dextea.trade.order.interfaces.http.dto.response;

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
@Schema(description = "获取月订单列表响应")
public class GetMonthOrdersResponse {

    @Schema(description = "月份订单列表")
    private List<MonthOrderItem> orders;

    @Schema(description = "当月订单总数", example = "12")
    private Integer totalCount;

    @Schema(description = "当月订单总金额（元）", example = "320.00")
    private BigDecimal totalAmount;
}
