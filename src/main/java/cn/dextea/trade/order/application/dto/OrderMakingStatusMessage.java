package cn.dextea.trade.order.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderMakingStatusMessage(
        Long orderId,
        String orderNo,
        Long storeId,
        int fromStatus,
        int toStatus,
        int makingStatus,
        int paymentStatus,
        String pickupCode,
        BigDecimal totalPrice,
        int totalQuantity,
        LocalDateTime createdAt) {

    public String toTag() {
        return fromStatus + "To" + toStatus;
    }
}
