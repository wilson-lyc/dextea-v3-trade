package cn.dextea.trade.service;

import cn.dextea.trade.model.CreateOrderRequest;
import cn.dextea.trade.model.CreateOrderResponse;
import cn.dextea.trade.model.PreBuildOrderRequest;
import cn.dextea.trade.model.PreBuildOrderResponse;

public interface OrderService {

    PreBuildOrderResponse preBuildOrder(PreBuildOrderRequest request);

    CreateOrderResponse createOrder(CreateOrderRequest request);
}
