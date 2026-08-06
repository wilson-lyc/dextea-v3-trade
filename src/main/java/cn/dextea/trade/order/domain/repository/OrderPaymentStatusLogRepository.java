package cn.dextea.trade.order.domain.repository;

import cn.dextea.trade.order.domain.model.OrderPaymentStatusLog;

import java.util.List;

public interface OrderPaymentStatusLogRepository {
    OrderPaymentStatusLog save(OrderPaymentStatusLog log);

    List<OrderPaymentStatusLog> saveAll(List<OrderPaymentStatusLog> logs);

    List<OrderPaymentStatusLog> listByOrderId(Long orderId);
}
