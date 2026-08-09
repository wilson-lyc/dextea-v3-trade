package cn.dextea.trade.order.application.dto;

import cn.dextea.trade.order.domain.enumeration.MakingStatus;

public record OrderMakingStatusMessage(String orderNo, MakingStatus fromStatus, MakingStatus toStatus) {

    public String toTag() {
        return fromStatus.name() + "To" + toStatus.name();
    }
}
