package cn.dextea.trade.order.domain.gateway;
import cn.dextea.trade.catalog.domain.model.aggregate.Store;
public interface StoreGateway {
    Store findStore(Long id);
}
