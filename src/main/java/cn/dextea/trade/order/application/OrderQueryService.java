package cn.dextea.trade.order.application;

import cn.dextea.trade.order.domain.model.OrderDetailView;
import cn.dextea.trade.order.domain.model.OrderSummaryView;

import java.util.List;

/**
 * 订单查询应用服务：编排订单列表与详情查询。
 */
public interface OrderQueryService {

    List<OrderSummaryView> getOrdersByCustomer(Long customerId);

    OrderDetailView getOrderDetail(Long orderId, Long customerId);
}
