package cn.dextea.trade.order.domain.port;

import cn.dextea.trade.order.domain.enumeration.MakingStatus;

public interface MakingStatusPublisher {

    void publishMakingStatusChange(String orderNo, MakingStatus fromStatus, MakingStatus toStatus);
}
