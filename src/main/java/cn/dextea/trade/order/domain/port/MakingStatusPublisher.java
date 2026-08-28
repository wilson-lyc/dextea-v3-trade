package cn.dextea.trade.order.domain.port;

import cn.dextea.trade.order.domain.enumeration.MakingStatus;
import cn.dextea.trade.order.domain.model.Order;

public interface MakingStatusPublisher {

    void publishMakingStatusChange(Order order, MakingStatus fromStatus, MakingStatus toStatus);
}
