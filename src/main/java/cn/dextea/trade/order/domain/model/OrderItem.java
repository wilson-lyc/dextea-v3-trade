package cn.dextea.trade.order.domain.model;

import cn.dextea.trade.shared.model.Money;
import cn.dextea.trade.shared.model.Quantity;

import lombok.Getter;

import java.util.List;

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

    public Money getTotalPrice() {
        if (quantity == null || unitPrice == null) {
            return Money.ZERO;
        }
        return unitPrice.multiply(quantity);
    }
}
