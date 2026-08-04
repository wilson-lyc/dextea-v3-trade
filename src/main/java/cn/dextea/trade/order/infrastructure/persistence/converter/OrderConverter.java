package cn.dextea.trade.order.infrastructure.persistence.converter;

import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.model.OrderItem;
import cn.dextea.trade.order.domain.model.enumeration.DiningMethod;
import cn.dextea.trade.order.domain.model.enumeration.MakingStatus;
import cn.dextea.trade.order.domain.model.enumeration.OrderSource;
import cn.dextea.trade.order.domain.model.enumeration.PaymentStatus;
import cn.dextea.trade.order.infrastructure.persistence.po.OrderItemPO;
import cn.dextea.trade.shared.domain.enumeration.PaymentMethod;
import cn.dextea.trade.order.infrastructure.persistence.po.OrderPO;
import cn.dextea.trade.shared.domain.enumeration.CodeEnum;
import cn.dextea.trade.shared.domain.model.Money;
import cn.dextea.trade.shared.domain.model.Quantity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderConverter {

    public OrderPO toOrderPO(Order order) {
        if (order == null) {
            return null;
        }
        OrderPO po = new OrderPO();
        po.setOrderNo(order.getOrderNo());
        po.setTradeNo(order.getTradeNo());
        po.setIdempotencyKey(order.getIdempotencyKey());
        po.setCustomerId(order.getCustomerId());
        po.setStoreId(order.getStoreId());
        po.setTotalPrice(order.getTotalPrice().getValue());
        po.setTotalQuantity(order.getTotalQuantity().getValue());
        po.setDiningMethod(toCode(order.getDiningMethod()));
        po.setNote(order.getNote());
        po.setSource(toCode(order.getSource()));
        po.setPickupCode(order.getPickupCode());
        po.setMakingStatus(toCode(order.getMakingStatus()));
        po.setPaymentMethod(toCode(order.getPaymentMethod()));
        po.setPaymentStatus(toCode(order.getPaymentStatus()));
        po.setPaymentExpiredAt(order.getPaymentExpiredAt());
        po.setPaymentPaidAt(order.getPaymentPaidAt());
        po.setPaymentRefundedAt(order.getPaymentRefundedAt());
        po.setVersion(order.getVersion() == null ? 1 : order.getVersion());
        return po;
    }

    public OrderItemPO toOrderItemPO(OrderItem item) {
        if (item == null) {
            return null;
        }
        OrderItemPO po = new OrderItemPO();
        po.setOrderId(item.getOrderId());
        po.setProductId(item.getProductId());
        po.setProductName(item.getProductName());
        po.setSkuId(item.getSkuId());
        po.setCustomization(item.getCustomization());
        po.setCoverUrl(item.getCoverUrl());
        po.setQuantity(item.getQuantity().getValue());
        po.setUnitPrice(item.getUnitPrice().getValue());
        po.setSubtotal(item.getTotalPrice().getValue());
        return po;
    }

    public Order toOrder(OrderPO po, List<OrderItem> items) {
        if (po == null) {
            return null;
        }
        return Order.reconstruct(
                po.getId(),
                po.getOrderNo(),
                po.getTradeNo(),
                po.getIdempotencyKey(),
                po.getCustomerId(),
                po.getStoreId(),
                po.getDiningMethod() == null ? null : DiningMethod.of(po.getDiningMethod()),
                po.getNote(),
                po.getSource() == null ? null : OrderSource.of(po.getSource()),
                po.getPickupCode(),
                po.getMakingStatus() == null ? null : MakingStatus.of(po.getMakingStatus()),
                po.getPaymentMethod() == null ? null : PaymentMethod.of(po.getPaymentMethod()),
                po.getPaymentStatus() == null ? null : PaymentStatus.of(po.getPaymentStatus()),
                po.getPaymentExpiredAt(),
                po.getPaymentPaidAt(),
                po.getPaymentRefundedAt(),
                po.getCreatedAt(),
                po.getUpdatedAt(),
                po.getVersion(),
                items);
    }

    public OrderItem toOrderItem(OrderItemPO po) {
        if (po == null) {
            return null;
        }
        return OrderItem.reconstruct(
                po.getId(),
                po.getOrderId(),
                po.getProductId(),
                po.getProductName(),
                po.getSkuId(),
                po.getCustomization(),
                po.getCoverUrl(),
                po.getQuantity() == null ? Quantity.ZERO : Quantity.of(po.getQuantity()),
                po.getUnitPrice() == null ? Money.ZERO : Money.of(po.getUnitPrice()),
                true);
    }

    private Integer toCode(CodeEnum codeEnum) {
        return codeEnum == null ? null : codeEnum.getCode();
    }
}
