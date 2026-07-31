package cn.dextea.trade.catalog.api.client;

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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface CatalogClient {
    List<Product> findProducts(List<Long> ids);

    Map<Long, ProductStoreStatus> findProductStoreStatusMap(List<Long> productIds, Long storeId);

    Map<Long, ProductCover> findProductCovers(List<Long> productIds);

    Map<Long, String> findCoverUrls(List<Long> coverIds);

    Map<Long, Customization> findCustomizationMap(Set<Long> ids);

    Map<Long, CustomizationOption> findOptionMap(Set<Long> ids);

    Map<Long, CustomizationOptionStoreStatus> findOptionStoreStatusMap(List<Long> optionIds, Long storeId);

    Store findStore(Long id);

    void assertStoreValid(Long storeId);

    Customer findCustomer(Long id);

    void assertCustomerValid(Long customerId);

    Map<Long, Product> findProductMap(Set<Long> productIds);

    Optional<UnavailableProduct> checkProductAvailability(
            Product product, ProductStoreStatus storeStatus, Long productId);

    List<UnavailableCustomization> checkOptionAvailability(
            List<Long> opts, List<Long> itemIdsForItem, Long productId, Product product,
            Map<Long, CustomizationOption> optionMap, Map<Long, Customization> customizationMap,
            Map<Long, CustomizationOptionStoreStatus> optionStoreStatusMap);
}
