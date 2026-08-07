package cn.dextea.trade.order.domain.model;

import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.enumeration.DiningMethod;
import cn.dextea.trade.order.domain.model.enumeration.MakingStatus;
import cn.dextea.trade.order.domain.model.enumeration.OrderSource;
import cn.dextea.trade.shared.enumeration.PaymentMethod;
import cn.dextea.trade.order.domain.model.enumeration.PaymentStatus;
import cn.dextea.trade.shared.error.BizError;
import cn.dextea.trade.shared.model.Money;
import cn.dextea.trade.shared.model.Quantity;

import cn.dextea.trade.order.domain.model.OrderPaymentStatusLog;
import cn.dextea.trade.order.domain.port.OrderNoGenerator;
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

    public static Order create(Long customerId, Long storeId) {
        Order order = new Order();
        order.customerId = customerId;
        order.storeId = storeId;
        return order;
    }

    public static Order create(Long customerId, Long storeId, OrderNoGenerator orderNoGenerator,
                                OrderSource source, PaymentMethod paymentMethod, DiningMethod diningMethod,
                                String note, String idempotencyKey) {
        Order order = create(customerId, storeId);
        order.orderNo = orderNoGenerator.next();
        order.source = source;
        order.paymentMethod = paymentMethod;
        order.diningMethod = diningMethod;
        order.note = note;
        order.idempotencyKey = idempotencyKey;
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

    public void initialize(String orderNo, OrderSource source, PaymentMethod paymentMethod,
                           DiningMethod diningMethod, String note, String idempotencyKey) {
        this.orderNo = orderNo;
        this.source = source;
        this.paymentMethod = paymentMethod;
        this.diningMethod = diningMethod;
        this.note = note;
        this.idempotencyKey = idempotencyKey;
    }

    public void save(OrderRepository orderRepository) {
        orderRepository.save(this);
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public void assignAmounts(Money totalPrice, Quantity totalQuantity) {
        this.totalPrice = totalPrice;
        this.totalQuantity = totalQuantity;
    }

    public void markCreated(String tradeNo, LocalDateTime paymentExpiredAt) {
        this.tradeNo = tradeNo;
        this.paymentExpiredAt = paymentExpiredAt;
        this.paymentStatus = PaymentStatus.PENDING;
        this.makingStatus = MakingStatus.PENDING;
        recordPaymentStatusChange(null, PaymentStatus.PENDING, "ORDER_CREATED");
    }

    public void markPaid(LocalDateTime paidAt) {
        this.paymentStatus = PaymentStatus.PAID;
        this.paymentPaidAt = paidAt;
        this.makingStatus = MakingStatus.PREPARING;
        recordPaymentStatusChange(PaymentStatus.PENDING, PaymentStatus.PAID, "ORDER_PAID");
    }

    public void markCancelled() {
        this.paymentStatus = PaymentStatus.CANCELLED;
        recordPaymentStatusChange(PaymentStatus.PENDING, PaymentStatus.CANCELLED, "ORDER_CANCELLED");
    }

    public boolean isPaid() {
        return paymentStatus == PaymentStatus.PAID;
    }

    public boolean isPendingPayment() {
        return paymentStatus == PaymentStatus.PENDING;
    }

    public boolean isCancelled() {
        return paymentStatus == PaymentStatus.CANCELLED;
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

    public void addItem(Product product, String skuId, Quantity quantity) {
        OrderItem orderItem = OrderItem.create(product, skuId, quantity);
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

    public void ensurePendingPayment() {
        if (!isPendingPayment()) {
            throw new BizError(OrderErrorCode.ORDER_CANNOT_CANCEL);
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
