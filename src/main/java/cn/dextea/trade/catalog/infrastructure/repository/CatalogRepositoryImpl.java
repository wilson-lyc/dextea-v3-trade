package cn.dextea.trade.catalog.infrastructure.repository;

import cn.dextea.trade.catalog.domain.model.aggregate.Customer;
import cn.dextea.trade.catalog.domain.model.aggregate.Product;
import cn.dextea.trade.catalog.domain.model.aggregate.Store;
import cn.dextea.trade.catalog.domain.model.valueobject.Customization;
import cn.dextea.trade.catalog.domain.model.valueobject.CustomizationOption;
import cn.dextea.trade.catalog.domain.model.valueobject.CustomizationOptionStoreStatus;
import cn.dextea.trade.catalog.domain.model.valueobject.ProductCover;
import cn.dextea.trade.catalog.domain.model.valueobject.ProductStoreStatus;
import cn.dextea.trade.catalog.domain.repository.CatalogRepository;
import cn.dextea.trade.catalog.infrastructure.gateway.CatalogGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CatalogRepositoryImpl implements CatalogRepository {
    private final CatalogGateway catalogGateway;

    @Override
    public List<Product> findProducts(List<Long> ids) {
        return catalogGateway.findProducts(ids);
    }

    @Override
    public List<ProductStoreStatus> findProductStoreStatus(List<Long> productIds, Long storeId) {
        return catalogGateway.findProductStoreStatus(productIds, storeId);
    }

    @Override
    public Map<Long, ProductCover> findProductCovers(List<Long> productIds) {
        return catalogGateway.findProductCovers(productIds);
    }

    @Override
    public Map<Long, String> findCoverUrls(List<Long> coverIds) {
        return catalogGateway.findCoverUrls(coverIds);
    }

    @Override
    public List<Customization> findCustomizations(List<Long> ids) {
        return catalogGateway.findCustomizations(ids);
    }

    @Override
    public List<CustomizationOption> findOptions(List<Long> ids) {
        return catalogGateway.findOptions(ids);
    }

    @Override
    public List<CustomizationOptionStoreStatus> findOptionStoreStatus(List<Long> optionIds, Long storeId) {
        return catalogGateway.findOptionStoreStatus(optionIds, storeId);
    }

    @Override
    public Store findStore(Long id) {
        return catalogGateway.findStore(id);
    }

    @Override
    public Customer findCustomer(Long id) {
        return catalogGateway.findCustomer(id);
    }
}
