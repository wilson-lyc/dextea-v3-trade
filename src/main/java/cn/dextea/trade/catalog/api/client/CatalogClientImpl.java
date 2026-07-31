package cn.dextea.trade.catalog.api.client;

import cn.dextea.trade.catalog.application.service.CatalogService;
import cn.dextea.trade.catalog.domain.model.aggregate.Customer;
import cn.dextea.trade.catalog.domain.model.aggregate.Product;
import cn.dextea.trade.catalog.domain.model.aggregate.Store;
import cn.dextea.trade.catalog.domain.model.valueobject.Customization;
import cn.dextea.trade.catalog.domain.model.valueobject.CustomizationOption;
import cn.dextea.trade.catalog.domain.model.valueobject.CustomizationOptionStoreStatus;
import cn.dextea.trade.catalog.domain.model.valueobject.ProductCover;
import cn.dextea.trade.catalog.domain.model.valueobject.ProductStoreStatus;
import cn.dextea.trade.catalog.domain.model.valueobject.UnavailableCustomization;
import cn.dextea.trade.catalog.domain.model.valueobject.UnavailableProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CatalogClientImpl implements CatalogClient {
    private final CatalogService catalogService;

    @Override
    public List<Product> findProducts(List<Long> ids) {
        return catalogService.findProducts(ids);
    }

    @Override
    public Map<Long, ProductStoreStatus> findProductStoreStatusMap(List<Long> productIds, Long storeId) {
        return catalogService.findProductStoreStatusMap(productIds, storeId);
    }

    @Override
    public Map<Long, ProductCover> findProductCovers(List<Long> productIds) {
        return catalogService.findProductCovers(productIds);
    }

    @Override
    public Map<Long, String> findCoverUrls(List<Long> coverIds) {
        return catalogService.findCoverUrls(coverIds);
    }

    @Override
    public Map<Long, Customization> findCustomizationMap(Set<Long> ids) {
        return catalogService.findCustomizationMap(ids);
    }

    @Override
    public Map<Long, CustomizationOption> findOptionMap(Set<Long> ids) {
        return catalogService.findOptionMap(ids);
    }

    @Override
    public Map<Long, CustomizationOptionStoreStatus> findOptionStoreStatusMap(List<Long> optionIds, Long storeId) {
        return catalogService.findOptionStoreStatusMap(optionIds, storeId);
    }

    @Override
    public Store findStore(Long id) {
        return catalogService.findStore(id);
    }

    @Override
    public void assertStoreValid(Long storeId) {
        catalogService.assertStoreValid(storeId);
    }

    @Override
    public Customer findCustomer(Long id) {
        return catalogService.findCustomer(id);
    }

    @Override
    public void assertCustomerValid(Long customerId) {
        catalogService.assertCustomerValid(customerId);
    }

    @Override
    public Map<Long, Product> findProductMap(Set<Long> productIds) {
        return catalogService.findProductMap(productIds);
    }

    @Override
    public Optional<UnavailableProduct> checkProductAvailability(
            Product product, ProductStoreStatus storeStatus, Long productId) {
        return catalogService.checkProductAvailability(product, storeStatus, productId);
    }

    @Override
    public List<UnavailableCustomization> checkOptionAvailability(
            List<Long> opts, List<Long> itemIdsForItem, Long productId, Product product,
            Map<Long, CustomizationOption> optionMap, Map<Long, Customization> customizationMap,
            Map<Long, CustomizationOptionStoreStatus> optionStoreStatusMap) {
        return catalogService.checkOptionAvailability(opts, itemIdsForItem, productId, product,
                optionMap, customizationMap, optionStoreStatusMap);
    }
}
