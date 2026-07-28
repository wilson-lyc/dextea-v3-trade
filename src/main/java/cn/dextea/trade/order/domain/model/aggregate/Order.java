package cn.dextea.trade.order.domain.model.aggregate;
import cn.dextea.trade.common.error.BizError;
import cn.dextea.trade.order.domain.enums.DiningMethodEnum;
import cn.dextea.trade.order.domain.enums.MakingStatusEnum;
import cn.dextea.trade.order.domain.enums.TradeStatusEnum;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.entity.OrderItem;
import cn.dextea.trade.order.domain.model.valueobject.PreBuildResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Long id;
    private String orderNo;
    private String tradeNo;
    private String idempotencyKey;
    private Long customerId;
    private Long storeId;
    private Integer tradeStatus;
    private Integer makingStatus;
    private Integer version;
    private String pickupCode;
    private BigDecimal totalPrice;
    private Integer totalQuantity;
    private Integer payMethod;
    private Integer diningMethod;
    private String note;
    private LocalDateTime payExpireAt;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private LocalDateTime updatedAt;
    private List<OrderItem> items;
    public static Order createInitial(String orderNo, String idempotencyKey, Long customerId, Long storeId,
                                    int payMethod, int diningMethod, String note,
                                    BigDecimal totalPrice, int totalQuantity,
                                    LocalDateTime payExpireAt, List<OrderItem> items) {
        if (DiningMethodEnum.of(diningMethod) == null) {
            throw new BizError(OrderErrorCode.DINING_METHOD_INVALID, "用餐方式错误: " + diningMethod);
        }
        if (items == null || items.isEmpty()) {
            throw new BizError(OrderErrorCode.ORDER_ITEMS_EMPTY, "订单明细不能为空");
        }
        if (totalPrice == null || totalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizError(OrderErrorCode.ORDER_PRICE_INVALID, "订单金额非法: " + totalPrice);
        }
        if (totalQuantity <= 0) {
            throw new BizError(OrderErrorCode.ORDER_QUANTITY_INVALID, "订单数量非法: " + totalQuantity);
        }
        if (payExpireAt == null || !payExpireAt.isAfter(LocalDateTime.now())) {
            throw new BizError(OrderErrorCode.ORDER_PAY_EXPIRE_AT_INVALID, "支付过期时间非法: " + payExpireAt);
        }
        return Order.builder()
                .orderNo(orderNo)
                .idempotencyKey(idempotencyKey)
                .customerId(customerId)
                .storeId(storeId)
                .tradeStatus(TradeStatusEnum.TRADE_WAIT_PAY.getCode())
                .makingStatus(MakingStatusEnum.MAKING_WAIT.getCode())
                .version(0)
                .payMethod(payMethod)
                .diningMethod(diningMethod)
                .note(note)
                .totalPrice(totalPrice)
                .totalQuantity(totalQuantity)
                .payExpireAt(payExpireAt)
                .items(items)
                .build();
    }
    public static Order createFromPreBuild(String orderNo, String idempotencyKey, Long customerId, Long storeId,
                                           int payMethod, int diningMethod, String note,
                                           Duration payTimeout, PreBuildResult preBuild) {
        List<OrderItem> items = preBuild.getProducts().stream()
                .map(p -> OrderItem.builder()
                        .productId(p.getProductId())
                        .skuId(p.getSkuId())
                        .productName(p.getProductName())
                        .coverId(p.getCoverId())
                        .customizationText(p.getCustomizationText())
                        .quantity(p.getQuantity())
                        .unitPrice(p.getUnitPrice())
                        .subtotal(p.getSubtotal())
                        .build())
                .toList();
        LocalDateTime payExpireAt = LocalDateTime.now().plus(payTimeout);
        return createInitial(orderNo, idempotencyKey, customerId, storeId,
                payMethod, diningMethod, note,
                preBuild.getTotalPrice(), preBuild.getTotalQuantity(),
                payExpireAt, items);
    }
    public void markTradeNo(String tradeNo) {
        if (this.tradeNo != null) {
            throw new BizError(OrderErrorCode.ORDER_TRADE_NO_ALREADY_SET, "trade_no 已存在，不可重复设置");
        }
        this.tradeNo = tradeNo;
    }
    public TradeStatusEnum tradeStatusEnum() {
        return TradeStatusEnum.of(this.tradeStatus);
    }
    public TradeStatusEnum markPaid(String tradeNo, LocalDateTime paidAt) {
        TradeStatusEnum target = transitionTo(TradeStatusEnum.TRADE_PAID, TradeStatusEnum.TRADE_WAIT_PAY);
        if (this.tradeNo == null) {
            this.tradeNo = tradeNo;
        }
        this.paidAt = paidAt;
        return target;
    }
    public TradeStatusEnum markPayTimeout() {
        return transitionTo(TradeStatusEnum.TRADE_PAY_TIMEOUT, TradeStatusEnum.TRADE_WAIT_PAY);
    }
    public TradeStatusEnum markRefunded(LocalDateTime refundedAt) {
        TradeStatusEnum target = transitionTo(TradeStatusEnum.TRADE_REFUNDED, TradeStatusEnum.TRADE_PAID);
        this.refundedAt = refundedAt;
        return target;
    }
    private TradeStatusEnum transitionTo(TradeStatusEnum target, TradeStatusEnum... allowedFrom) {
        TradeStatusEnum current = tradeStatusEnum();
        for (TradeStatusEnum from : allowedFrom) {
            if (current == from) {
                this.tradeStatus = target.getCode();
                return target;
            }
        }
        throw new BizError(OrderErrorCode.ORDER_STATUS_TRANSITION_INVALID,
                String.format("非法状态流转：%s → %s", current, target));
    }
}
