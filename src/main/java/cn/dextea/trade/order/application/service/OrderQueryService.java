package cn.dextea.trade.order.application.service;
import cn.dextea.trade.order.application.dto.OrderDetailDTO;
import cn.dextea.trade.order.application.dto.OrderStatusDTO;
import cn.dextea.trade.order.application.dto.OrderSummaryDTO;
import java.util.List;
public interface OrderQueryService {
    List<OrderSummaryDTO> getOrdersByCustomer(Long customerId, int year, int month);
    OrderDetailDTO getOrderDetail(Long orderId, Long customerId);
    OrderStatusDTO getOrderStatus(Long orderId, Long customerId);
}
