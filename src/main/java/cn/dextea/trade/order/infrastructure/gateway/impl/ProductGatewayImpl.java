package cn.dextea.trade.order.infrastructure.gateway.impl;

import cn.dextea.trade.catalog.api.client.CatalogClient;
import cn.dextea.trade.catalog.domain.model.aggregate.Product;
import cn.dextea.trade.catalog.domain.model.valueobject.ProductCover;
import cn.dextea.trade.catalog.domain.model.valueobject.ProductStoreStatus;
import cn.dextea.trade.order.domain.gateway.ProductGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProductGatewayImpl implements ProductGateway {
    private final CatalogClient catalogClient;

    @Override
    public List<Product> findProducts(List<Long> ids) {
        return catalogClient.findProducts(ids);
    }

    @Override
    public List<ProductStoreStatus> findProductStoreStatus(List<Long> productIds, Long storeId) {
        return catalogClient.findProductStoreStatusMap(productIds, storeId).values().stream().toList();
    }

    @Override
    public Map<Long, ProductCover> findProductCovers(List<Long> productIds) {
        return catalogClient.findProductCovers(productIds);
    }

    @Override
    public Map<Long, String> findCoverUrls(List<Long> coverIds) {
        return catalogClient.findCoverUrls(coverIds);
    }
}
