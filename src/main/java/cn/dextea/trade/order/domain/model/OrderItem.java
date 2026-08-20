package cn.dextea.trade.order.domain.model;

import cn.dextea.trade.shared.model.Money;
import cn.dextea.trade.shared.model.Quantity;

import lombok.Getter;

@Getter
public class OrderItem {
    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    private String skuId;
    private String customization;
    private String coverUrl;
    private Quantity quantity;
    private Money unitPrice;
    private Boolean available;

    private OrderItem() {
    }

    public static OrderItem create(Long productId, String productName, String skuId, String customization,
            String coverUrl, Quantity quantity, Money unitPrice, Boolean available) {
        if (productId == null) {
            throw new IllegalArgumentException("productId 不能为空");
        }
        if (quantity == null || quantity.getValue() <= 0) {
            throw new IllegalArgumentException("商品数量必须大于 0");
        }
        if (unitPrice == null || unitPrice.isNegative()) {
            throw new IllegalArgumentException("单价不能为空且不能为负");
        }
        OrderItem orderItem = new OrderItem();
        orderItem.productId = productId;
        orderItem.productName = productName;
        orderItem.skuId = skuId;
        orderItem.customization = customization;
        orderItem.coverUrl = coverUrl;
        orderItem.quantity = quantity;
        orderItem.unitPrice = unitPrice;
        orderItem.available = available;
        return orderItem;
    }

    public static OrderItem reconstruct(Long id, Long orderId, Long productId, String productName,
            String skuId, String customization, String coverUrl,
            Quantity quantity, Money unitPrice, Boolean available) {
        if (productId == null) {
            throw new IllegalArgumentException("productId 不能为空");
        }
        if (quantity == null || quantity.getValue() <= 0) {
            throw new IllegalArgumentException("商品数量必须大于 0");
        }
        if (unitPrice == null || unitPrice.isNegative()) {
            throw new IllegalArgumentException("单价不能为空且不能为负");
        }
        OrderItem orderItem = new OrderItem();
        orderItem.id = id;
        orderItem.orderId = orderId;
        orderItem.productId = productId;
        orderItem.productName = productName;
        orderItem.skuId = skuId;
        orderItem.customization = customization;
        orderItem.coverUrl = coverUrl;
        orderItem.quantity = quantity;
        orderItem.unitPrice = unitPrice;
        orderItem.available = available;
        return orderItem;
    }

    public void assignOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public boolean isAvailable() {
        return Boolean.TRUE.equals(available);
    }

    public Money getTotalPrice() {
        return unitPrice.multiply(quantity);
    }
}
