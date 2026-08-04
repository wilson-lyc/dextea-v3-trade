package cn.dextea.trade.order.domain.port;

import cn.dextea.trade.order.domain.dto.CreateTradeRequest;
import cn.dextea.trade.order.domain.dto.QueryTradeResult;

public interface PaymentPort {
    String createTradeNo(CreateTradeRequest request);

    QueryTradeResult queryTrade(String outTradeNo);
}
