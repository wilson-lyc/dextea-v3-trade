package cn.dextea.trade.order.infrastructure.persistence.converter;

import cn.dextea.trade.order.domain.model.OrderPaymentStatusLog;
import cn.dextea.trade.order.infrastructure.persistence.po.OrderPaymentStatusLogPO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderPaymentStatusLogConverter {

    public OrderPaymentStatusLogPO toPO(OrderPaymentStatusLog log) {
        if (log == null) {
            return null;
        }
        OrderPaymentStatusLogPO po = new OrderPaymentStatusLogPO();
        po.setId(log.getId());
        po.setOrderId(log.getOrderId() == null ? null : String.valueOf(log.getOrderId()));
        po.setFromStatus(log.getFromStatus());
        po.setToStatus(log.getToStatus());
        po.setEvent(log.getEvent());
        po.setVersion(log.getVersion() == null ? 1 : log.getVersion());
        po.setCreatedAt(log.getCreatedAt());
        return po;
    }

    public OrderPaymentStatusLog toModel(OrderPaymentStatusLogPO po) {
        if (po == null) {
            return null;
        }
        return OrderPaymentStatusLog.builder()
                .id(po.getId())
                .orderId(po.getOrderId() == null ? null : Long.valueOf(po.getOrderId()))
                .fromStatus(po.getFromStatus())
                .toStatus(po.getToStatus())
                .event(po.getEvent())
                .version(po.getVersion())
                .createdAt(po.getCreatedAt())
                .build();
    }

    public List<OrderPaymentStatusLog> toModels(List<OrderPaymentStatusLogPO> pos) {
        if (pos == null || pos.isEmpty()) {
            return List.of();
        }
        return pos.stream().map(this::toModel).collect(Collectors.toList());
    }
}
