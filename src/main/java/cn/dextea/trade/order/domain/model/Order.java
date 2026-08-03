package cn.dextea.trade.order.domain.model;

import cn.dextea.trade.order.domain.model.Product;
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
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
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

    public static Order prebuild(Long customerId, Long storeId) {
        requireValid(customerId, storeId);
        Order order = new Order();
        order.customerId = customerId;
        order.storeId = storeId;
        return order;
    }

    public static Order create(OrderNoGenerator orderNoGenerator, Long customerId, Long storeId) {
        if (orderNoGenerator == null) {
            throw new IllegalArgumentException("orderNoGenerator must not be null when creating an order");
        }
        requireValid(customerId, storeId);
        Order order = new Order();
        order.customerId = customerId;
        order.storeId = storeId;
        order.orderNo = orderNoGenerator.next();
        return order;
    }

    public OrderItem addItem(Product product, String skuId, Quantity quantity) {
        if (product == null) {
            throw new IllegalArgumentException("product must not be null when adding an order item");
        }
        if (skuId == null || skuId.isEmpty()) {
            throw new IllegalArgumentException("skuId must not be null or empty when adding an order item");
        }
        if (quantity == null || quantity.equals(Quantity.ZERO)) {
            throw new IllegalArgumentException("quantity must not be null or zero when adding an order item");
        }

        AtomicBoolean available = new AtomicBoolean(product.isActive());
        String customization = product.resolveCustomization(skuId, available);

        OrderItem orderItem = OrderItem.builder()
                .productId(product.getId())
                .productName(product.getName())
                .skuId(skuId)
                .customization(customization)
                .cover(product.getCover() != null ? product.getCover().getUrl() : null)
                .quantity(quantity)
                .unitPrice(product.getPrice())
                .available(available.get())
                .build();

        items.add(orderItem);
        return orderItem;
    }

    private static void requireValid(Long customerId, Long storeId) {
        if (storeId == null) {
            throw new IllegalArgumentException("storeId must not be null when creating an order");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("customerId must not be null when creating an order");
        }
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public boolean hasItems() {
        return !items.isEmpty();
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
