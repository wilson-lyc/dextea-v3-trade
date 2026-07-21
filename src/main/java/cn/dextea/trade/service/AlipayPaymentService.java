package cn.dextea.trade.service;

import cn.dextea.trade.entity.Order;

/**
 * 支付宝支付能力：封装 alipay.trade.create 交易创建。
 */
public interface AlipayPaymentService {

    /**
     * 调用 alipay.trade.create 创建交易，返回支付宝交易号 trade_no。
     *
     * @param order       订单（使用 orderNo 作为 out_trade_no，price 作为总金额）
     * @param buyerOpenId 买家在支付宝的 openid（取自顾客表）
     * @return 支付宝交易号 trade_no
     */
    String createTrade(Order order, String buyerOpenId);
}
