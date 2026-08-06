package cn.dextea.trade.order.infrastructure.persistence.converter;

import cn.dextea.trade.order.domain.model.OrderMakingStatusLog;
import cn.dextea.trade.order.infrastructure.persistence.po.OrderMakingStatusLogPO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMakingStatusLogConverter {

    public OrderMakingStatusLogPO toPO(OrderMakingStatusLog log) {
        if (log == null) {
            return null;
        }
        OrderMakingStatusLogPO po = new OrderMakingStatusLogPO();
        po.setId(log.getId());
        po.setOrderId(log.getOrderId() == null ? null : String.valueOf(log.getOrderId()));
        po.setFromStatus(log.getFromStatus());
        po.setToStatus(log.getToStatus());
        po.setEvent(log.getEvent());
        po.setVersion(log.getVersion() == null ? 1 : log.getVersion());
        po.setCreatedAt(log.getCreatedAt());
        return po;
    }

    public OrderMakingStatusLog toModel(OrderMakingStatusLogPO po) {
        if (po == null) {
            return null;
        }
        return OrderMakingStatusLog.builder()
                .id(po.getId())
                .orderId(po.getOrderId() == null ? null : Long.valueOf(po.getOrderId()))
                .fromStatus(po.getFromStatus())
                .toStatus(po.getToStatus())
                .event(po.getEvent())
                .version(po.getVersion())
                .createdAt(po.getCreatedAt())
                .build();
    }

    public List<OrderMakingStatusLog> toModels(List<OrderMakingStatusLogPO> pos) {
        if (pos == null || pos.isEmpty()) {
            return List.of();
        }
        return pos.stream().map(this::toModel).collect(Collectors.toList());
    }
}
