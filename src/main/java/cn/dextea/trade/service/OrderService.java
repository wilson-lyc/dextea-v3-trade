package cn.dextea.trade.service;

import cn.dextea.trade.model.CreateOrderRequest;
import cn.dextea.trade.model.CreateOrderResponse;
import cn.dextea.trade.model.OrderDetailResponse;
import cn.dextea.trade.model.OrderSummary;
import cn.dextea.trade.model.PreBuildOrderRequest;
import cn.dextea.trade.model.PreBuildOrderResponse;

import java.util.List;

public interface OrderService {

    PreBuildOrderResponse preBuildOrder(PreBuildOrderRequest request);

    CreateOrderResponse createOrder(CreateOrderRequest request);

    List<OrderSummary> getOrdersByCustomer(Long customerId);

    OrderDetailResponse getOrderDetail(Long orderId, Long customerId);
}
