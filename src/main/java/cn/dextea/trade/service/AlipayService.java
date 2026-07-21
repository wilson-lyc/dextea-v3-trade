package cn.dextea.trade.service;

import cn.dextea.trade.dto.CreateAlipayTradeRequest;

public interface AlipayService {
    String createTrade(CreateAlipayTradeRequest request);
}
