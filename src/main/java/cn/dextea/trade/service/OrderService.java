package cn.dextea.trade.service;

import cn.dextea.trade.dto.OrderCalculateRequest;
import cn.dextea.trade.dto.OrderCalculateResult;

public interface OrderService {

    OrderCalculateResult calculate(OrderCalculateRequest request);
}
