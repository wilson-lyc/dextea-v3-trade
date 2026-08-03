package cn.dextea.trade.order.domain.port;

import cn.dextea.trade.order.domain.dto.CreateTradeRequest;

public interface PaymentPort {
    String createTradeNo(CreateTradeRequest request);
}
