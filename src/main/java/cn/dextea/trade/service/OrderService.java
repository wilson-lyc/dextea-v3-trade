package cn.dextea.trade.service;

import cn.dextea.trade.model.CreateOrderRequest;
import cn.dextea.trade.model.CreateOrderResponse;
import cn.dextea.trade.model.PreBuildOrderResponse;

public interface OrderService {

    PreBuildOrderResponse preBuildOrder(CreateOrderRequest request);

    CreateOrderResponse createOrder(CreateOrderRequest request);
}
