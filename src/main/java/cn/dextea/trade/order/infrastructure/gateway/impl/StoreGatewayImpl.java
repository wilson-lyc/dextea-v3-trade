package cn.dextea.trade.order.infrastructure.gateway.impl;

import cn.dextea.trade.catalog.api.client.CatalogClient;
import cn.dextea.trade.catalog.domain.model.aggregate.Store;
import cn.dextea.trade.order.domain.gateway.StoreGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StoreGatewayImpl implements StoreGateway {
    private final CatalogClient catalogClient;

    @Override
    public Store findStore(Long id) {
        return catalogClient.findStore(id);
    }
}
