package cn.dextea.trade.order.domain.gateway;
import cn.dextea.trade.order.domain.model.valueobject.Store;
public interface StoreGateway {
    Store findStore(Long id);
}
