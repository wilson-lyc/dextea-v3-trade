package cn.dextea.trade.order.application.dto;

public record OrderMakingStatusMessage(Long orderId, Long storeId, int fromStatus, int toStatus) {

    public String toTag() {
        return fromStatus + "To" + toStatus;
    }
}
