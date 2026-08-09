package cn.dextea.trade.order.domain.model;

import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.enumeration.DiningMethod;
import cn.dextea.trade.order.domain.enumeration.MakingStatus;
import cn.dextea.trade.order.domain.enumeration.OrderSource;
import cn.dextea.trade.shared.enumeration.PaymentMethod;
import cn.dextea.trade.order.domain.enumeration.PaymentStatus;
import cn.dextea.trade.shared.error.BizError;
import cn.dextea.trade.shared.model.Money;
import cn.dextea.trade.shared.model.Quantity;

import cn.dextea.trade.order.domain.repository.OrderRepository;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class Order {
    private Long id;
    private String orderNo;
    private String tradeNo;
    private String idempotencyKey;
    private Long customerId;
    private Long storeId;
    private DiningMethod diningMethod;
    private String note;
    private OrderSource source;
    private String pickupCode;
    private MakingStatus makingStatus;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDateTime paymentExpiredAt;
    private LocalDateTime paymentPaidAt;
    private LocalDateTime paymentRefundedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer version;
    private Money totalPrice;
    private Quantity totalQuantity;
    private List<OrderItem> items;
    private List<OrderPaymentStatusLog> paymentStatusLogs;

    private Order() {
        this.items = new ArrayList<>();
    }

    public static Order createDraft(Long customerId, Long storeId) {
        Order order = new Order();
        order.customerId = customerId;
        order.storeId = storeId;
        return order;
    }

    public static Order reconstruct(Long id, String orderNo, String tradeNo, String idempotencyKey,
                                    Long customerId, Long storeId, DiningMethod diningMethod, String note,
                                    OrderSource source, String pickupCode, MakingStatus makingStatus,
                                    PaymentMethod paymentMethod, PaymentStatus paymentStatus,
                                    LocalDateTime paymentExpiredAt, LocalDateTime paymentPaidAt,
                                    LocalDateTime paymentRefundedAt, LocalDateTime createdAt,
                                    LocalDateTime updatedAt, Integer version,
                                    Money totalPrice, Quantity totalQuantity, List<OrderItem> items) {
        Order order = new Order();
        order.id = id;
        order.orderNo = orderNo;
        order.tradeNo = tradeNo;
        order.idempotencyKey = idempotencyKey;
        order.customerId = customerId;
        order.storeId = storeId;
        order.diningMethod = diningMethod;
        order.note = note;
        order.source = source;
        order.pickupCode = pickupCode;
        order.makingStatus = makingStatus;
        order.paymentMethod = paymentMethod;
        order.paymentStatus = paymentStatus;
        order.paymentExpiredAt = paymentExpiredAt;
        order.paymentPaidAt = paymentPaidAt;
        order.paymentRefundedAt = paymentRefundedAt;
        order.createdAt = createdAt;
        order.updatedAt = updatedAt;
        order.version = version;
        order.totalPrice = totalPrice;
        order.totalQuantity = totalQuantity;
        order.items = items == null ? new ArrayList<>() : items;
        return order;
    }

    public void place(String orderNo, OrderSource source, PaymentMethod paymentMethod,
                       DiningMethod diningMethod, String note, String idempotencyKey,
                       Money totalPrice, Quantity totalQuantity) {
        this.orderNo = orderNo;
        this.source = source;
        this.paymentMethod = paymentMethod;
        this.diningMethod = diningMethod;
        this.note = note;
        this.idempotencyKey = idempotencyKey;
        this.totalPrice = totalPrice;
        this.totalQuantity = totalQuantity;
    }

    public void save(OrderRepository orderRepository) {
        orderRepository.save(this);
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public void markCreated(String tradeNo, LocalDateTime paymentExpiredAt) {
        this.tradeNo = tradeNo;
        this.paymentExpiredAt = paymentExpiredAt;
        this.paymentStatus = PaymentStatus.PENDING;
        this.makingStatus = MakingStatus.PENDING;
        recordPaymentStatusChange(null, PaymentStatus.PENDING, "ORDER_CREATED");
    }

    public void assignPickupCode(String pickupCode) {
        this.pickupCode = pickupCode;
    }

    public void setTradeNoIfAbsent(String tradeNo) {
        if (this.tradeNo == null && tradeNo != null) {
            this.tradeNo = tradeNo;
        }
    }

    public void markPaid(LocalDateTime paidAt) {
        ensureCanMarkPaid();
        this.paymentStatus = PaymentStatus.PAID;
        this.paymentPaidAt = paidAt;
        this.makingStatus = MakingStatus.PREPARING;
        recordPaymentStatusChange(PaymentStatus.PENDING, PaymentStatus.PAID, "ORDER_PAID");
    }

    public void markPaymentTimeout() {
        ensureCanMarkPaymentTimeout();
        this.paymentStatus = PaymentStatus.TIMEOUT;
        this.makingStatus = MakingStatus.CANCELLED;
        recordPaymentStatusChange(PaymentStatus.PENDING, PaymentStatus.TIMEOUT, "ORDER_PAYMENT_TIMEOUT");
    }

    public void markCancelled() {
        ensureCanMarkCancelled();
        PaymentStatus from = this.paymentStatus;
        this.paymentStatus = PaymentStatus.CANCELLED;
        this.makingStatus = MakingStatus.CANCELLED;
        recordPaymentStatusChange(from, PaymentStatus.CANCELLED, "ORDER_CANCELLED");
    }

    public boolean isPendingPayment() {
        return isPaymentStatus(PaymentStatus.PENDING);
    }

    public boolean isPaid() {
        return isPaymentStatus(PaymentStatus.PAID);
    }

    public boolean isPaymentTimeout() {
        return isPaymentStatus(PaymentStatus.TIMEOUT);
    }

    public boolean isCancelled() {
        return isPaymentStatus(PaymentStatus.CANCELLED);
    }

    public boolean isRefunding() {
        return isPaymentStatus(PaymentStatus.REFUNDING);
    }

    public boolean isRefunded() {
        return isPaymentStatus(PaymentStatus.REFUNDED);
    }

    private boolean isPaymentStatus(PaymentStatus status) {
        return paymentStatus == status;
    }

    public boolean canMarkPaid() {
        return isPendingPayment();
    }

    public boolean canMarkPaymentTimeout() {
        return isPendingPayment();
    }

    public boolean canMarkCancelled() {
        return isPendingPayment() || isPaymentTimeout();
    }

    public void ensureCanMarkPaid() {
        if (!canMarkPaid()) {
            throw new BizError(OrderErrorCode.ORDER_CANNOT_PAID);
        }
    }

    public void ensureCanMarkPaymentTimeout() {
        if (!canMarkPaymentTimeout()) {
            throw new BizError(OrderErrorCode.ORDER_CANNOT_TIMEOUT);
        }
    }

    public void ensureCanMarkCancelled() {
        if (!canMarkCancelled()) {
            throw new BizError(OrderErrorCode.ORDER_CANNOT_CANCEL);
        }
    }

    public void ensurePendingPayment() {
        if (!isPendingPayment()) {
            throw new BizError(OrderErrorCode.ORDER_CANNOT_CANCEL);
        }
    }

    public void markReady() {
        ensureCanMarkReady();
        this.makingStatus = MakingStatus.READY;
    }

    public void markCollected() {
        ensureCanMarkCollected();
        this.makingStatus = MakingStatus.COLLECTED;
    }

    public boolean isPreparing() {
        return isMakingStatus(MakingStatus.PREPARING);
    }

    public boolean isReady() {
        return isMakingStatus(MakingStatus.READY);
    }

    public boolean isCollected() {
        return isMakingStatus(MakingStatus.COLLECTED);
    }

    private boolean isMakingStatus(MakingStatus status) {
        return makingStatus == status;
    }

    public boolean canMarkReady() {
        return isPaid() && isPreparing();
    }

    public boolean canMarkCollected() {
        return isPaid() && isReady();
    }

    public void ensurePaid() {
        if (!isPaid()) {
            throw new BizError(OrderErrorCode.ORDER_NOT_PAID);
        }
    }

    public void ensurePreparing() {
        if (!isPreparing()) {
            throw new BizError(OrderErrorCode.ORDER_NOT_PREPARING);
        }
    }

    public void ensureReady() {
        if (!isReady()) {
            throw new BizError(OrderErrorCode.ORDER_NOT_READY);
        }
    }

    public void ensureCanMarkReady() {
        if (!canMarkReady()) {
            throw new BizError(OrderErrorCode.ORDER_INVALID_MAKING_TRANSITION);
        }
    }

    public void ensureCanMarkCollected() {
        if (!canMarkCollected()) {
            throw new BizError(OrderErrorCode.ORDER_INVALID_MAKING_TRANSITION);
        }
    }

    private void recordPaymentStatusChange(PaymentStatus from, PaymentStatus to, String event) {
        if (paymentStatusLogs == null) {
            paymentStatusLogs = new ArrayList<>();
        }
        paymentStatusLogs.add(OrderPaymentStatusLog.builder()
                .fromStatus(from == null ? null : from.getCode())
                .toStatus(to == null ? null : to.getCode())
                .event(event)
                .createdAt(LocalDateTime.now())
                .build());
    }

    public List<OrderPaymentStatusLog> pullPaymentStatusLogs() {
        if (paymentStatusLogs == null || paymentStatusLogs.isEmpty()) {
            return Collections.emptyList();
        }
        List<OrderPaymentStatusLog> logs = paymentStatusLogs;
        paymentStatusLogs = null;
        return logs;
    }

    public void assignAmounts(Money totalPrice, Quantity totalQuantity) {
        this.totalPrice = totalPrice;
        this.totalQuantity = totalQuantity;
    }

    public void addItem(OrderItem orderItem) {
        items.add(orderItem);
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public boolean hasItems() {
        return !items.isEmpty();
    }

    public boolean belongsTo(Long customerId) {
        return customerId != null && customerId.equals(this.customerId);
    }

    public void ensureBelongsTo(Long customerId) {
        if (!belongsTo(customerId)) {
            throw new BizError(OrderErrorCode.ORDER_NOT_BELONG_TO_CUSTOMER);
        }
    }

    public boolean isInitialized() {
        return orderNo != null;
    }

    public Money getTotalPrice() {
        return totalPrice == null ? Money.ZERO : totalPrice;
    }

    public Quantity getTotalQuantity() {
        return totalQuantity == null ? Quantity.ZERO : totalQuantity;
    }
}
