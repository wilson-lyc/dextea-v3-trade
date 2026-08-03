package cn.dextea.trade.order.infrastructure.persistence.converter;

import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.model.OrderItem;
import cn.dextea.trade.order.infrastructure.persistence.po.OrderItemPO;
import cn.dextea.trade.order.infrastructure.persistence.po.OrderPO;
import cn.dextea.trade.shared.domain.enumeration.CodeEnum;
import org.springframework.stereotype.Component;

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
        po.setCoverId(item.getCoverId());
        po.setQuantity(item.getQuantity().getValue());
        po.setUnitPrice(item.getUnitPrice().getValue());
        po.setSubtotal(item.getTotalPrice().getValue());
        return po;
    }

    private Integer toCode(CodeEnum codeEnum) {
        return codeEnum == null ? null : codeEnum.getCode();
    }
}
