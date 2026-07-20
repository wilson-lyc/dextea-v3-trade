package cn.dextea.trade.service;

import cn.dextea.trade.dto.CreateOrderRequest;
import cn.dextea.trade.dto.OrderCalculateResponse;

public interface OrderService {

    /**
     * 计算确认订单页的订单金额，并剔除不可用商品与客制化选项。
     *
     * @param request 计算请求（门店、就餐方式、商品列表）
     * @return 计算结果（不可用清单、有效商品总数量与总金额）
     */
    OrderCalculateResponse calculate(CreateOrderRequest request);
}
