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
@Schema(description = "订单详情响应")
public class OrderDetailResponse {

    @Schema(description = "订单ID", example = "1")
    private Long id;

    @Schema(description = "订单编号", example = "DX202604230001")
    private String orderNo;

    @Schema(description = "交易流水号", example = "202604231234567890")
    private String tradeNo;

    @Schema(description = "顾客ID", example = "10086")
    private Long customerId;

    @Schema(description = "门店ID", example = "1")
    private Long storeId;

    @Schema(description = "门店名称", example = "朝阳旗舰店")
    private String storeName;

    @Schema(description = "用餐方式", example = "0")
    private Integer diningMethod;

    @Schema(description = "备注", example = "少冰")
    private String note;

    @Schema(description = "订单来源", example = "0")
    private Integer source;

    @Schema(description = "取餐码", example = "A12")
    private String pickupCode;

    @Schema(description = "制作状态", example = "0")
    private Integer makingStatus;

    @Schema(description = "支付方式", example = "0")
    private Integer paymentMethod;

    @Schema(description = "支付状态", example = "1")
    private Integer paymentStatus;

    @Schema(description = "支付过期时间")
    private LocalDateTime paymentExpiredAt;

    @Schema(description = "支付完成时间")
    private LocalDateTime paymentPaidAt;

    @Schema(description = "退款时间")
    private LocalDateTime paymentRefundedAt;

    @Schema(description = "下单时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "订单总价（元）", example = "36.00")
    private BigDecimal totalPrice;

    @Schema(description = "订单商品总数量", example = "2")
    private Integer totalQuantity;

    @Schema(description = "订单项列表")
    private List<OrderDetailItem> items;
}
