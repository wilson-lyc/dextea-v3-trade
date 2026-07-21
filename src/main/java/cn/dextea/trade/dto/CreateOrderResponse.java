package cn.dextea.trade.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建订单响应")
public class CreateOrderResponse {

    @Schema(description = "订单ID", example = "1")
    private Long id;

    @Schema(description = "订单号（内部雪花单号，创建成功时返回）", example = "1789000000000000001")
    private String orderNo;

    @Schema(description = "交易号（支付宝支付渠道回传，创建订单时尚未生成，为空）")
    private String tradeNo;

    @Schema(description = "订单商品总数量", example = "2")
    private Integer totalQuantity;

    @Schema(description = "订单总价（元）", example = "25.00")
    private BigDecimal totalPrice;

    @Schema(description = "不可用的商品与客制化")
    private CreateOrderUnavailable unavailable;
}
