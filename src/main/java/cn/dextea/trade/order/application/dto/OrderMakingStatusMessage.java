package cn.dextea.trade.order.application.dto;

import cn.dextea.trade.order.domain.enumeration.MakingStatus;

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
        return nameOf(fromStatus) + "_TO_" + nameOf(toStatus);
    }

    private String nameOf(int status) {
        MakingStatus makingStatus = MakingStatus.of(status);
        return makingStatus == null ? "UNKNOWN_" + status : makingStatus.name();
    }
}
