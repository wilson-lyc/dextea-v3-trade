package cn.dextea.trade.order.interfaces.http.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "获取月订单列表请求")
public class GetMonthOrdersRequest {

    @NotNull(message = "year 不能为空")
    @Schema(description = "年份", example = "2026")
    private Integer year;

    @NotNull(message = "month 不能为空")
    @Min(value = 1, message = "month 必须在 1-12 之间")
    @Max(value = 12, message = "month 必须在 1-12 之间")
    @Schema(description = "月份（1-12）", example = "4")
    private Integer month;
}
