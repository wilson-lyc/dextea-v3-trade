package cn.dextea.trade.order.application.service;

import cn.dextea.trade.order.application.dto.OrderDetailDTO;
import cn.dextea.trade.order.application.dto.OrderSummaryDTO;

import java.util.List;

/**
 * 订单查询应用服务：编排订单列表与详情查询（读路径绕过领域服务）。
 */
public interface OrderQueryService {

    List<OrderSummaryDTO> getOrdersByCustomer(Long customerId);

    OrderDetailDTO getOrderDetail(Long orderId, Long customerId);
}
