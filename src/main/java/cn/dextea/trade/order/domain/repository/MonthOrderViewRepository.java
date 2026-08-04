package cn.dextea.trade.order.domain.repository;

import cn.dextea.trade.order.domain.model.MonthOrderView;

import java.util.List;
import java.util.Map;

public interface MonthOrderViewRepository {

    Map<Long, MonthOrderView> findViews(List<Long> orderIds);
}
