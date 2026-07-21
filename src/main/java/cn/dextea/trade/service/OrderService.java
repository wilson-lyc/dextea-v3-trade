package cn.dextea.trade.service;

import cn.dextea.trade.dto.CreateOrderRequest;
import cn.dextea.trade.dto.CreateOrderResponse;
import cn.dextea.trade.dto.PreBuildOrderResponse;

public interface OrderService {

    PreBuildOrderResponse preBuildOrder(CreateOrderRequest request);

    CreateOrderResponse createOrder(CreateOrderRequest request);
}
