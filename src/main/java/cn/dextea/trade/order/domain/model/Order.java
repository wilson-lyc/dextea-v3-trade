package cn.dextea.trade.order.domain.model;

import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.enumeration.DiningMethod;
import cn.dextea.trade.order.domain.model.enumeration.MakingStatus;
import cn.dextea.trade.order.domain.model.enumeration.OrderSource;
import cn.dextea.trade.shared.domain.enumeration.PaymentMethod;
import cn.dextea.trade.order.domain.model.enumeration.PaymentStatus;
import cn.dextea.trade.shared.domain.error.BizError;
import cn.dextea.trade.shared.domain.model.Money;
import cn.dextea.trade.shared.domain.model.Quantity;

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
    private List<OrderItem> items;

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

    public void save(OrderRepository orderRepository) {
        orderRepository.save(this);
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

    public void assignId(Long id) {
        this.id = id;
    }

    public void markCreated(String tradeNo, LocalDateTime paymentExpiredAt) {
        this.tradeNo = tradeNo;
        this.paymentExpiredAt = paymentExpiredAt;
        this.paymentStatus = PaymentStatus.PENDING;
        this.makingStatus = MakingStatus.PENDING;
    }

    public void markPaid(LocalDateTime paidAt) {
        this.paymentStatus = PaymentStatus.PAID;
        this.paymentPaidAt = paidAt;
    }

    public boolean isPaid() {
        return paymentStatus == PaymentStatus.PAID;
    }

    /**
     * 是否处于「未支付」状态，即支付结果尚未确定、仍可能被支付渠道回填。
     * 该状态下允许主动向支付渠道查询交易结果做对账补偿。
     */
    public boolean isPendingPayment() {
        return paymentStatus == PaymentStatus.PENDING;
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

    public boolean isInitialized() {
        return orderNo != null;
    }

    public Money getTotalPrice() {
        Money total = Money.ZERO;
        for (OrderItem item : items) {
            if (item.getAvailable() == null || !item.getAvailable()) {
                continue;
            }
            total = total.add(item.getTotalPrice());
        }
        return total;
    }

    public Quantity getTotalQuantity() {
        Quantity total = Quantity.ZERO;
        for (OrderItem item : items) {
            if (item.getAvailable() == null || !item.getAvailable()) {
                continue;
            }
            if (item.getQuantity() == null) {
                continue;
            }
            total = total.add(item.getQuantity());
        }
        return total;
    }

    public static Order reconstruct(Long id, String orderNo, String tradeNo, String idempotencyKey,
                                    Long customerId, Long storeId, DiningMethod diningMethod, String note,
                                    OrderSource source, String pickupCode, MakingStatus makingStatus,
                                    PaymentMethod paymentMethod, PaymentStatus paymentStatus,
                                    LocalDateTime paymentExpiredAt, LocalDateTime paymentPaidAt,
                                    LocalDateTime paymentRefundedAt, LocalDateTime createdAt,
                                    LocalDateTime updatedAt, Integer version, List<OrderItem> items) {
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
        order.items = items == null ? new ArrayList<>() : items;
        return order;
    }
}
