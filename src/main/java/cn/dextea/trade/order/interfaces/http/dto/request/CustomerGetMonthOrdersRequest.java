package cn.dextea.trade.order.interfaces.http.dto.request;

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
public class CustomerGetMonthOrdersRequest {

    @NotNull(message = "year 不能为空")
    private Integer year;

    @NotNull(message = "month 不能为空")
    @Min(value = 1, message = "month 必须在 1-12 之间")
    @Max(value = 12, message = "month 必须在 1-12 之间")
    private Integer month;
}
