package cn.dextea.trade.service;

import cn.dextea.trade.dto.CreateOrderRequest;
import cn.dextea.trade.dto.CreateOrderResponse;
import cn.dextea.trade.dto.CalculateOrderResponse;

public interface OrderService {

    /**
     * 计算确认订单页的订单金额，并剔除不可用商品与客制化选项。
     *
     * @param request 计算请求（门店、就餐方式、商品列表）
     * @return 计算结果（不可用清单、有效商品总数量与总金额）
     */
    CalculateOrderResponse calculate(CreateOrderRequest request);

    /**
     * 创建订单：用雪花算法生成订单号并落库，返回订单 ID 与交易号。
     *
     * <p>交易号 {@code tradeNo} 暂用订单号 {@code orderNo} 代替，
     * 待接入微信/支付宝支付渠道后替换为渠道返回的真实交易号。</p>
     *
     * @param request 创建订单请求（门店、用户、支付平台、就餐方式、商品列表）
     * @return 创建结果（订单 ID、交易号）
     */
    CreateOrderResponse createOrder(CreateOrderRequest request);
}

