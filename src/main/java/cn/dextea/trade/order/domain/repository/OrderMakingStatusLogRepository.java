package cn.dextea.trade.order.domain.repository;

import cn.dextea.trade.order.domain.model.OrderMakingStatusLog;

import java.util.List;

public interface OrderMakingStatusLogRepository {
    OrderMakingStatusLog save(OrderMakingStatusLog log);

    List<OrderMakingStatusLog> saveAll(List<OrderMakingStatusLog> logs);

    List<OrderMakingStatusLog> listByOrderId(Long orderId);
}
