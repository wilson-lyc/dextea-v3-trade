package cn.dextea.trade.order.domain.repository;

import cn.dextea.trade.order.domain.model.Store;

public interface StoreRepository {
    Store getStoreById(Long id);
}
