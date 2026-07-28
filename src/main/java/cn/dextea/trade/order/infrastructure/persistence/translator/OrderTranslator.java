package cn.dextea.trade.order.infrastructure.persistence.translator;
import cn.dextea.trade.order.domain.model.aggregate.Order;
import cn.dextea.trade.order.domain.model.entity.OrderItem;
import cn.dextea.trade.order.domain.model.entity.OrderStatusLog;
import cn.dextea.trade.order.infrastructure.persistence.po.OrderItemPO;
import cn.dextea.trade.order.infrastructure.persistence.po.OrderPO;
import cn.dextea.trade.order.infrastructure.persistence.po.OrderStatusLogPO;
import java.util.List;
import java.util.stream.Collectors;
public final class OrderTranslator {
    private OrderTranslator() {
    }
    public static OrderPO toOrderPO(Order order) {
        if (order == null) {
            return null;
        }
        return OrderPO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .tradeNo(order.getTradeNo())
                .idempotencyKey(order.getIdempotencyKey())
                .customerId(order.getCustomerId())
                .storeId(order.getStoreId())
                .tradeStatus(order.getTradeStatus())
                .makingStatus(order.getMakingStatus())
                .version(order.getVersion())
                .pickupCode(order.getPickupCode())
                .totalPrice(order.getTotalPrice())
                .totalQuantity(order.getTotalQuantity())
                .payMethod(order.getPayMethod())
                .diningMethod(order.getDiningMethod())
                .note(order.getNote())
                .payExpireAt(order.getPayExpireAt())
                .createdAt(order.getCreatedAt())
                .paidAt(order.getPaidAt())
                .refundedAt(order.getRefundedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
    public static Order toOrder(OrderPO po) {
        if (po == null) {
            return null;
        }
        Order order = Order.builder()
                .id(po.getId())
                .orderNo(po.getOrderNo())
                .tradeNo(po.getTradeNo())
                .idempotencyKey(po.getIdempotencyKey())
                .customerId(po.getCustomerId())
                .storeId(po.getStoreId())
                .tradeStatus(po.getTradeStatus())
                .makingStatus(po.getMakingStatus())
                .version(po.getVersion())
                .pickupCode(po.getPickupCode())
                .totalPrice(po.getTotalPrice())
                .totalQuantity(po.getTotalQuantity())
                .payMethod(po.getPayMethod())
                .diningMethod(po.getDiningMethod())
                .note(po.getNote())
                .payExpireAt(po.getPayExpireAt())
                .createdAt(po.getCreatedAt())
                .paidAt(po.getPaidAt())
                .refundedAt(po.getRefundedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
        return order;
    }
    public static List<Order> toOrders(List<OrderPO> pos) {
        return pos == null ? List.of() : pos.stream().map(OrderTranslator::toOrder).collect(Collectors.toList());
    }
    public static OrderItemPO toOrderItemPO(OrderItem item) {
        if (item == null) {
            return null;
        }
        return OrderItemPO.builder()
                .id(item.getId())
                .orderId(item.getOrderId())
                .productId(item.getProductId())
                .skuId(item.getSkuId())
                .productName(item.getProductName())
                .coverId(item.getCoverId())
                .customizationText(item.getCustomizationText())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
    public static OrderItem toOrderItem(OrderItemPO po) {
        if (po == null) {
            return null;
        }
        return OrderItem.builder()
                .id(po.getId())
                .orderId(po.getOrderId())
                .productId(po.getProductId())
                .skuId(po.getSkuId())
                .productName(po.getProductName())
                .coverId(po.getCoverId())
                .customizationText(po.getCustomizationText())
                .quantity(po.getQuantity())
                .unitPrice(po.getUnitPrice())
                .subtotal(po.getSubtotal())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }
    public static List<OrderItemPO> toOrderItemPOs(List<OrderItem> items) {
        return items == null ? List.of()
                : items.stream().map(OrderTranslator::toOrderItemPO).collect(Collectors.toList());
    }
    public static List<OrderItem> toOrderItems(List<OrderItemPO> pos) {
        return pos == null ? List.of() : pos.stream().map(OrderTranslator::toOrderItem).collect(Collectors.toList());
    }
    public static OrderStatusLogPO toOrderStatusLogPO(OrderStatusLog log) {
        if (log == null) {
            return null;
        }
        return OrderStatusLogPO.builder()
                .id(log.getId())
                .orderId(log.getOrderId())
                .fromStatus(log.getFromStatus())
                .toStatus(log.getToStatus())
                .event(log.getEvent())
                .operator(log.getOperator())
                .version(log.getVersion())
                .createdAt(log.getCreatedAt())
                .build();
    }
    public static OrderStatusLog toOrderStatusLog(OrderStatusLogPO po) {
        if (po == null) {
            return null;
        }
        return OrderStatusLog.builder()
                .id(po.getId())
                .orderId(po.getOrderId())
                .fromStatus(po.getFromStatus())
                .toStatus(po.getToStatus())
                .event(po.getEvent())
                .operator(po.getOperator())
                .version(po.getVersion())
                .createdAt(po.getCreatedAt())
                .build();
    }
}
