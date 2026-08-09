package cn.dextea.trade.shared.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderPaidEvent(
        String orderNo,
        String tradeNo,
        String platform,
        BigDecimal amount) {
}
