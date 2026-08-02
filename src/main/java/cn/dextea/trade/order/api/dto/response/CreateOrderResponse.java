package cn.dextea.trade.order.api.dto.response;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "创建订单响应")
public class CreateOrderResponse extends AbstractCreateOrderResponse {
    @Schema(description = "订单ID", example = "1")
    private Long id;
    @Schema(description = "订单号", example = "20150320010101001")
    private String orderNo;
    @Schema(description = "交易号", example = "2015042321001004720200028594")
    private String tradeNo;
    @Schema(description = "支付过期时间点（系统计算并已同步支付宝，前端可据此做支付倒计时）",
            example = "2026-04-23T15:45:00")
    private LocalDateTime payExpireAt;
}
