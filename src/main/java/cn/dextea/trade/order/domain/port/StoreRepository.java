package cn.dextea.trade.order.domain.port;

import cn.dextea.trade.order.domain.model.aggregate.Store;

public interface StoreRepository {

    Store findStore(Long id);
}
