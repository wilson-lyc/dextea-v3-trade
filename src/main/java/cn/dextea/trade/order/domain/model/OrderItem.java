package cn.dextea.trade.order.domain.model;

import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.shared.domain.error.BizError;
import cn.dextea.trade.shared.domain.money.Money;
import cn.dextea.trade.shared.domain.quantity.Quantity;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public class OrderItem {
    private Long id;
    private Long productId;
    private String productName;
    private String skuId;
    private String customization;
    private String cover;
    private Quantity quantity;
    private Money unitPrice;
    private Boolean available;

    private OrderItem() {
    }

    public static OrderItem create(Product product, String skuId, Quantity quantity) {
        if (product == null) {
            throw new BizError(OrderErrorCode.PRODUCT_NOT_FOUND);
        }
        if (skuId == null || skuId.isEmpty()) {
            throw new BizError(OrderErrorCode.INVALID_SKU);
        }
        if (quantity == null || quantity.equals(Quantity.ZERO)) {
            throw new BizError(OrderErrorCode.INVALID_ORDER_ITEM_QUANTITY);
        }

        AtomicBoolean available = new AtomicBoolean(product.isActive());
        String customization = product.resolveCustomization(skuId, available);

        OrderItem orderItem = new OrderItem();
        orderItem.productId = product.getId();
        orderItem.productName = product.getName();
        orderItem.skuId = skuId;
        orderItem.customization = customization;
        orderItem.cover = product.getCover() != null ? product.getCover().getUrl() : null;
        orderItem.quantity = quantity;
        orderItem.unitPrice = product.getPrice();
        orderItem.available = available.get();
        return orderItem;
    }

    public Money getTotalPrice() {
        if (quantity == null || unitPrice == null) {
            return Money.ZERO;
        }
        return unitPrice.multiply(quantity);
    }
}
