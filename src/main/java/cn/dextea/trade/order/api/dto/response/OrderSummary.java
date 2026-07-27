package cn.dextea.trade.order.api.dto.response;

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
@Schema(description = "订单概要信息")
public class OrderSummary {

    @Schema(description = "门店名称", example = "杭州西湖店")
    private String storeName;

    @Schema(description = "下单时间", example = "2026-04-23T15:30:00")
    private LocalDateTime orderTime;

    @Schema(description = "交易状态（支付维度）：0-待支付 1-已支付 2-支付超时 3-退款中 4-已退款", example = "1")
    private Integer tradeStatus;

    @Schema(description = "交易状态文案", example = "已支付")
    private String tradeStatusDesc;

    @Schema(description = "制作进度状态：0-待制作 1-制作中 2-制作完成 3-已交付", example = "0")
    private Integer makingStatus;

    @Schema(description = "制作进度文案", example = "待制作")
    private String makingStatusDesc;

    @Schema(description = "订单总价", example = "99.00")
    private BigDecimal totalPrice;

    @Schema(description = "商品总数量", example = "3")
    private Integer totalQuantity;

    @Schema(description = "所购商品封面图 URL 列表")
    private List<String> coverUrls;
}
