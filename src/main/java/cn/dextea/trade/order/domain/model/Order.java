package cn.dextea.trade.order.domain.model;

import cn.dextea.trade.order.domain.model.enums.DiningMethod;
import cn.dextea.trade.order.domain.model.enums.MakingStatus;
import cn.dextea.trade.order.domain.model.enums.OrderSource;
import cn.dextea.trade.order.domain.model.enums.PaymentMethod;
import cn.dextea.trade.order.domain.model.enums.PaymentStatus;
import cn.dextea.trade.shared.domain.money.Money;
import cn.dextea.trade.shared.domain.quantity.Quantity;

import cn.dextea.trade.order.domain.port.OrderNoGenerator;

import lombok.Getter;

import java.time.LocalDateTime;
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

    private Order(Long customerId, Long storeId, List<OrderItem> items) {
        this.customerId = customerId;
        this.storeId = storeId;
        this.items = items == null ? Collections.emptyList() : items;
    }

    public static Order prebuild(Long customerId, Long storeId, List<OrderItem> items) {
        requireValid(customerId, storeId, items);
        return new Order(customerId, storeId, items);
    }

    public static Order create(OrderNoGenerator orderNoGenerator, Long customerId, Long storeId, List<OrderItem> items) {
        if (orderNoGenerator == null) {
            throw new IllegalArgumentException("orderNoGenerator must not be null when creating an order");
        }
        requireValid(customerId, storeId, items);
        Order order = new Order(customerId, storeId, items);
        order.orderNo = orderNoGenerator.next();
        return order;
    }

    private static void requireValid(Long customerId, Long storeId, List<OrderItem> items) {
        if (storeId == null) {
            throw new IllegalArgumentException("storeId must not be null when creating an order");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("customerId must not be null when creating an order");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("order must contain at least one item");
        }
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public Money getTotalPrice() {
        Money total = Money.ZERO;
        for (OrderItem item : items) {
            total = total.add(item.getTotalPrice());
        }
        return total;
    }

    public Quantity getTotalQuantity() {
        Quantity total = Quantity.ZERO;
        for (OrderItem item : items) {
            if (item.getQuantity() == null) {
                continue;
            }
            total = total.add(item.getQuantity());
        }
        return total;
    }
}
