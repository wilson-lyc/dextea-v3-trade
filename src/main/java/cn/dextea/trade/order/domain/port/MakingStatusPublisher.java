package cn.dextea.trade.order.domain.port;

import cn.dextea.trade.order.domain.enumeration.MakingStatus;

public interface MakingStatusPublisher {

    void publishMakingStatusChange(Long orderId, Long storeId, MakingStatus fromStatus, MakingStatus toStatus);
}
