package cn.dextea.trade.order.domain.port;

import java.util.List;
import java.util.Map;

public interface MonthOrderViewRepository {

    Map<Long, String> findStoreNames(List<Long> orderIds);
}
