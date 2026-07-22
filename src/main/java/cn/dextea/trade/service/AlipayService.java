package cn.dextea.trade.service;

import cn.dextea.trade.model.CreateAlipayTradeRequest;

public interface AlipayService {
    String createTrade(CreateAlipayTradeRequest request);
}
