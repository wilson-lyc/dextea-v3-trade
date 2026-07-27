package cn.dextea.trade.order.domain.gateway;

import cn.dextea.trade.order.domain.model.valueobject.Store;

/**
 * 门店网关：提供门店只读快照，用于下单前门店可用性校验与详情展示。
 */
public interface StoreGateway {

    Store findStore(Long id);
}
