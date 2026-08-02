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
    @Schema(description = "订单号", example = "20150320010101001")
    private String orderNo;
    @Schema(description = "交易号", example = "2015042321001004720200028594")
    private String tradeNo;
    @Schema(description = "交易状态（支付维度）：0-待支付 1-已支付 2-支付超时 3-退款中 4-已退款", example = "1")
    private Integer tradeStatus;
    @Schema(description = "制作进度状态：0-待制作 1-制作中 2-制作完成 3-已交付", example = "0")
    private Integer makingStatus;
    @Schema(description = "取餐码（支付成功后生成，未支付为 null）", example = "8011")
    private String pickupCode;
    @Schema(description = "订单总价", example = "99.00")
    private BigDecimal totalPrice;
    @Schema(description = "商品总数量", example = "3")
    private Integer totalQuantity;
    @Schema(description = "支付方式：0-未指定 1-微信支付 2-支付宝 3-银行卡", example = "2")
    private Integer payMethod;
    @Schema(description = "用餐方式：0-堂食 1-外带", example = "0")
    private Integer diningMethod;
    @Schema(description = "订单备注", example = "少冰少糖")
    private String note;
    @Schema(description = "支付过期时间点（系统计算并已同步支付宝，待支付订单前端可据此做倒计时）",
            example = "2026-04-23T15:45:00")
    private LocalDateTime payExpireAt;
    @Schema(description = "下单时间", example = "2026-04-23T15:30:00")
    private LocalDateTime createdAt;
    @Schema(description = "支付时间", example = "2026-04-23T15:35:00")
    private LocalDateTime paidAt;
    @Schema(description = "退款时间", example = "2026-04-24T10:00:00")
    private LocalDateTime refundedAt;
    @Schema(description = "更新时间", example = "2026-04-23T15:35:00")
    private LocalDateTime updatedAt;
    @Schema(description = "门店ID", example = "1")
    private Long storeId;
    @Schema(description = "门店名称", example = "杭州西湖店")
    private String storeName;
    @Schema(description = "商品明细列表")
    private List<OrderDetailItem> items;
}
