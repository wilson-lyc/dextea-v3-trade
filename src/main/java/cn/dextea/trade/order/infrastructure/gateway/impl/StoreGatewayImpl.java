package cn.dextea.trade.order.infrastructure.gateway.impl;
import cn.dextea.trade.order.domain.gateway.StoreGateway;
import cn.dextea.trade.order.domain.model.valueobject.Store;
import cn.dextea.trade.order.infrastructure.gateway.mapper.CatalogMapper;
import cn.dextea.trade.order.infrastructure.gateway.translator.StoreTranslator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
public class StoreGatewayImpl implements StoreGateway {
    private final CatalogMapper catalogMapper;
    @Override
    public Store findStore(Long id) {
        if (id == null) {
            return null;
        }
        return StoreTranslator.toStore(catalogMapper.selectStoreById(id));
    }
}
