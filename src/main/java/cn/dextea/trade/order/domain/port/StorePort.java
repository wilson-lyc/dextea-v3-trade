package cn.dextea.trade.order.domain.port;

import cn.dextea.trade.catalog.domain.model.Store;

/**
 * 门店防腐端口（只读）。
 */
public interface StorePort {

    Store findById(Long id);
}
