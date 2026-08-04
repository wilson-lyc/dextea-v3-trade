package cn.dextea.trade.order.domain.repository;

import cn.dextea.trade.order.domain.model.Store;

import java.util.Collection;
import java.util.Map;

public interface StoreRepository {
    Store getStoreById(Long id);

    Map<Long, Store> getStoresByIds(Collection<Long> storeIds);
}
