package cn.dextea.trade.order.interfaces.http.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetStoreWindowOrdersRequest {

    @NotNull(message = "小时窗口不能为空")
    private Integer hours;
}
