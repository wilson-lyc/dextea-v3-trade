package cn.dextea.trade.order.interfaces.http.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "月订单列表中的单条订单")
public class MonthOrderItem {

    @Schema(description = "订单ID", example = "1")
    private Long id;

    @Schema(description = "门店名称", example = "朝阳旗舰店")
    private String storeName;

    @Schema(description = "下单时间", example = "2026-04-23T15:45:00")
    private LocalDateTime createdAt;

    @Schema(description = "订单总价（元）", example = "25.00")
    private BigDecimal totalPrice;

    @Schema(description = "订单商品总数量", example = "2")
    private Integer totalQuantity;

    @Schema(description = "制作状态", example = "0")
    private Integer makingStatus;

    @Schema(description = "支付状态", example = "1")
    private Integer paymentStatus;

    @Schema(description = "商品封面图列表", example = "[\"https://example.com/a.jpg\"]")
    private List<String> covers;
}
