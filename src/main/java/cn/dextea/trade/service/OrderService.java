package cn.dextea.trade.service;

import cn.dextea.trade.dto.CreateOrderRequest;
import cn.dextea.trade.dto.CreateOrderResponse;
import cn.dextea.trade.dto.PreBuildOrderResponse;

public interface OrderService {

    /**
     * 预构建订单：校验门店ID、顾客ID、商品ID、客制化项目ID与客制化选项ID 的合法性，
     * 计算订单价格与商品数量，并剔除不可用商品与客制化选项。
     *
     * <p>任一ID不合法时抛出业务异常（message 形如「门店ID错误: 1001」）。
     * 该逻辑被 {@link #createOrder(CreateOrderRequest)} 复用，确保下单前数据有效合法。</p>
     *
     * @param request 预构建请求（门店、顾客、就餐方式、商品列表）
     * @return 预构建结果（不可用清单、有效商品总数量与总金额）
     */
    PreBuildOrderResponse preBuildOrder(CreateOrderRequest request);

    /**
     * 创建订单：用雪花算法生成订单号并落库，返回订单 ID 与交易号。
     *
     * <p>创建前复用 {@link #preBuildOrder(CreateOrderRequest)} 完成数据校验、价格计算与数量统计；
     * 存在不可用项时不落库，id 与 tradeNo 置空返回。</p>
     *
     * <p>交易号 {@code tradeNo} 暂用订单号 {@code orderNo} 代替，
     * 待接入微信/支付宝支付渠道后替换为渠道返回的真实交易号。</p>
     *
     * @param request 创建订单请求（门店、用户、支付平台、就餐方式、商品列表）
     * @return 创建结果（订单 ID、交易号）
     */
    CreateOrderResponse createOrder(CreateOrderRequest request);
}
