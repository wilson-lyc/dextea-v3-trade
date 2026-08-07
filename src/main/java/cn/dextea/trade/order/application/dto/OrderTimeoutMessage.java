package cn.dextea.trade.order.application.dto;

import java.time.LocalDateTime;

public record OrderTimeoutMessage(String orderNo, LocalDateTime paymentExpiredAt) {
}
