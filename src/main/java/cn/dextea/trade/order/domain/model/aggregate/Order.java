package cn.dextea.trade.order.domain.model.aggregate;

import cn.dextea.trade.common.error.BizError;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.entity.OrderItem;
import cn.dextea.trade.order.domain.model.valueobject.DiningMethod;
import cn.dextea.trade.order.domain.model.valueobject.MakingStatus;
import cn.dextea.trade.order.domain.model.valueobject.OrderNumber;
import cn.dextea.trade.order.domain.model.valueobject.PaymentMethod;
import cn.dextea.trade.order.domain.model.valueobject.PaymentStatus;
import cn.dextea.trade.order.domain.model.valueobject.PickupCode;
import cn.dextea.trade.order.domain.model.valueobject.PreBuildResult;
import cn.dextea.trade.shared.domain.money.Money;
import cn.dextea.trade.shared.domain.quantity.Quantity;
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
    private OrderNumber orderNo;
    private String tradeNo;
    private String idempotencyKey;
    private Long customerId;
    private Long storeId;
    private Money totalPrice;
    private Quantity totalQuantity;
    private DiningMethod diningMethod;
    private String note;
    private PickupCode pickupCode;
    private MakingStatus makingStatus;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDateTime paymentExpiredAt;
    private LocalDateTime paymentPaidAt;
    private LocalDateTime paymentRefundedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer version;
    private List<OrderItem> items;

    public static Order createInitial(OrderNumber orderNo, String idempotencyKey, Long customerId, Long storeId,
            PaymentMethod paymentMethod, DiningMethod diningMethod, String note,
            BigDecimal totalPrice, int totalQuantity,
            LocalDateTime paymentExpiredAt, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new BizError(OrderErrorCode.ORDER_ITEMS_EMPTY, "订单明细不能为空");
        }
        Money money;
        try {
            money = Money.of(totalPrice);
        } catch (IllegalArgumentException e) {
            throw new BizError(OrderErrorCode.ORDER_PRICE_EMPTY, e.getMessage());
        }
        Quantity quantity;
        try {
            quantity = Quantity.of(totalQuantity);
        } catch (IllegalArgumentException e) {
            throw new BizError(OrderErrorCode.ORDER_QUANTITY_INVALID, e.getMessage());
        }
        if (paymentExpiredAt == null || !paymentExpiredAt.isAfter(LocalDateTime.now())) {
            throw new BizError(OrderErrorCode.ORDER_PAY_EXPIRE_AT_INVALID, "支付过期时间非法: " + paymentExpiredAt);
        }
        return Order.builder()
                .orderNo(orderNo)
                .idempotencyKey(idempotencyKey)
                .customerId(customerId)
                .storeId(storeId)
                .paymentStatus(PaymentStatus.PENDING)
                .makingStatus(MakingStatus.PENDING)
                .version(0)
                .paymentMethod(paymentMethod)
                .diningMethod(diningMethod)
                .note(note)
                .totalPrice(money)
                .totalQuantity(quantity)
                .paymentExpiredAt(paymentExpiredAt)
                .items(items)
                .build();
    }

    public static Order createFromPreBuild(OrderNumber orderNo, String idempotencyKey, Long customerId, Long storeId,
            PaymentMethod paymentMethod, int diningMethod, String note,
            Duration payTimeout, PreBuildResult preBuild) {
        DiningMethod method;
        try {
            method = DiningMethod.of(diningMethod);
        } catch (IllegalArgumentException e) {
            throw new BizError(OrderErrorCode.DINING_METHOD_INVALID, e.getMessage());
        }
        List<OrderItem> items = preBuild.getProducts().stream()
                .map(p -> OrderItem.builder()
                        .productId(p.getProductId())
                        .productName(p.getProductName())
                        .skuId(p.getSkuId())
                        .customization(p.getCustomizationText())
                        .coverId(p.getCoverId())
                        .quantity(Quantity.of(p.getQuantity()))
                        .unitPrice(Money.of(p.getUnitPrice()))
                        .subtotal(Money.of(p.getSubtotal()))
                        .build())
                .toList();
        LocalDateTime paymentExpiredAt = LocalDateTime.now().plus(payTimeout);
        return createInitial(orderNo, idempotencyKey, customerId, storeId,
                paymentMethod, method, note,
                preBuild.getTotalPrice(), preBuild.getTotalQuantity(),
                paymentExpiredAt, items);
    }

    public void markTradeNo(String tradeNo) {
        if (this.tradeNo != null) {
            throw new BizError(OrderErrorCode.ORDER_TRADE_NO_ALREADY_SET, "trade_no 已存在，不可重复设置");
        }
        this.tradeNo = tradeNo;
    }

    public PaymentStatus paymentStatus() {
        return paymentStatus;
    }

    public PaymentStatus markPaid(String tradeNo, LocalDateTime paymentPaidAt) {
        PaymentStatus target = transitionTo(PaymentStatus.PAID, PaymentStatus.PENDING);
        if (this.tradeNo == null) {
            this.tradeNo = tradeNo;
        }
        this.paymentPaidAt = paymentPaidAt;
        return target;
    }

    public PaymentStatus markPayTimeout() {
        return transitionTo(PaymentStatus.PAY_TIMEOUT, PaymentStatus.PENDING);
    }

    public PaymentStatus markRefunded(LocalDateTime paymentRefundedAt) {
        PaymentStatus target = transitionTo(PaymentStatus.REFUNDED, PaymentStatus.PAID, PaymentStatus.REFUNDING);
        this.paymentRefundedAt = paymentRefundedAt;
        return target;
    }

    private PaymentStatus transitionTo(PaymentStatus target, PaymentStatus... allowedFrom) {
        PaymentStatus current = paymentStatus;
        for (PaymentStatus from : allowedFrom) {
            if (current == from) {
                this.paymentStatus = target;
                return target;
            }
        }
        throw new BizError(OrderErrorCode.ORDER_STATUS_TRANSITION_INVALID,
                String.format("非法状态流转：%s → %s", current, target));
    }
}
