package cn.dextea.trade.order.infrastructure.persistence.translator;
import cn.dextea.trade.order.domain.model.aggregate.Order;
import cn.dextea.trade.order.domain.model.entity.OrderItem;
import cn.dextea.trade.order.domain.model.entity.OrderStatusLog;
import cn.dextea.trade.order.domain.model.valueobject.DiningMethod;
import cn.dextea.trade.order.domain.model.valueobject.MakingStatus;
import cn.dextea.trade.order.domain.model.valueobject.OrderNumber;
import cn.dextea.trade.order.domain.model.valueobject.PaymentMethod;
import cn.dextea.trade.order.domain.model.valueobject.PaymentStatus;
import cn.dextea.trade.order.domain.model.valueobject.PickupCode;
import cn.dextea.trade.order.infrastructure.persistence.po.OrderItemPO;
import cn.dextea.trade.order.infrastructure.persistence.po.OrderPO;
import cn.dextea.trade.order.infrastructure.persistence.po.OrderStatusLogPO;
import cn.dextea.trade.shared.domain.money.Money;
import cn.dextea.trade.shared.domain.quantity.Quantity;
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
                .orderNo(order.getOrderNo() == null ? null : order.getOrderNo().getValue())
                .tradeNo(order.getTradeNo())
                .idempotencyKey(order.getIdempotencyKey())
                .customerId(order.getCustomerId())
                .storeId(order.getStoreId())
                .tradeStatus(order.getPaymentStatus() == null ? null : order.getPaymentStatus().getCode())
                .makingStatus(order.getMakingStatus() == null ? null : order.getMakingStatus().getCode())
                .version(order.getVersion())
                .pickupCode(order.getPickupCode() == null ? null : order.getPickupCode().getValue())
                .totalPrice(order.getTotalPrice() == null ? null : order.getTotalPrice().getValue())
                .totalQuantity(order.getTotalQuantity() == null ? null : order.getTotalQuantity().getValue())
                .payMethod(order.getPaymentMethod() == null ? null : order.getPaymentMethod().getCode())
                .diningMethod(order.getDiningMethod() == null ? null : order.getDiningMethod().getCode())
                .note(order.getNote())
                .payExpireAt(order.getPaymentExpiredAt())
                .createdAt(order.getCreatedAt())
                .paidAt(order.getPaymentPaidAt())
                .refundedAt(order.getPaymentRefundedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
    public static Order toOrder(OrderPO po) {
        if (po == null) {
            return null;
        }
        return Order.builder()
                .id(po.getId())
                .orderNo(po.getOrderNo() == null ? null : OrderNumber.of(po.getOrderNo()))
                .tradeNo(po.getTradeNo())
                .idempotencyKey(po.getIdempotencyKey())
                .customerId(po.getCustomerId())
                .storeId(po.getStoreId())
                .paymentStatus(po.getTradeStatus() == null ? null : PaymentStatus.of(po.getTradeStatus()))
                .makingStatus(po.getMakingStatus() == null ? null : MakingStatus.of(po.getMakingStatus()))
                .version(po.getVersion())
                .pickupCode(PickupCode.of(po.getPickupCode()))
                .totalPrice(po.getTotalPrice() == null ? null : Money.of(po.getTotalPrice()))
                .totalQuantity(po.getTotalQuantity() == null ? null : Quantity.of(po.getTotalQuantity()))
                .paymentMethod(po.getPayMethod() == null ? null : PaymentMethod.of(po.getPayMethod()))
                .diningMethod(po.getDiningMethod() == null ? null : DiningMethod.of(po.getDiningMethod()))
                .note(po.getNote())
                .paymentExpiredAt(po.getPayExpireAt())
                .createdAt(po.getCreatedAt())
                .paymentPaidAt(po.getPaidAt())
                .paymentRefundedAt(po.getRefundedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
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
                .productName(item.getProductName())
                .skuId(item.getSkuId())
                .customization(item.getCustomization())
                .coverId(item.getCoverId())
                .quantity(item.getQuantity() == null ? null : item.getQuantity().getValue())
                .unitPrice(item.getUnitPrice() == null ? null : item.getUnitPrice().getValue())
                .subtotal(item.getSubtotal() == null ? null : item.getSubtotal().getValue())
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
                .productName(po.getProductName())
                .skuId(po.getSkuId())
                .customization(po.getCustomization())
                .coverId(po.getCoverId())
                .quantity(po.getQuantity() == null ? null : Quantity.of(po.getQuantity()))
                .unitPrice(po.getUnitPrice() == null ? null : Money.of(po.getUnitPrice()))
                .subtotal(po.getSubtotal() == null ? null : Money.of(po.getSubtotal()))
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
