package cn.dextea.trade.service;

import cn.dextea.trade.enums.OrderEventEnum;

import java.time.LocalDateTime;

public interface OrderStatusService {

    void changeStatus(String orderNo, OrderEventEnum event, String operator,
                      String tradeNo, LocalDateTime paidAt, LocalDateTime refundedAt);

    default void changeStatus(String orderNo, OrderEventEnum event, String operator) {
        changeStatus(orderNo, event, operator, null, null, null);
    }
}
