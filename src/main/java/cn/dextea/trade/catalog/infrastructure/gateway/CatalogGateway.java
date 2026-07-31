package cn.dextea.trade.catalog.infrastructure.gateway;

import cn.dextea.trade.catalog.domain.model.aggregate.Customer;
import cn.dextea.trade.catalog.domain.model.aggregate.Product;
import cn.dextea.trade.catalog.domain.model.aggregate.Store;
import cn.dextea.trade.catalog.domain.model.valueobject.Customization;
import cn.dextea.trade.catalog.domain.model.valueobject.CustomizationOption;
import cn.dextea.trade.catalog.domain.model.valueobject.CustomizationOptionStoreStatus;
import cn.dextea.trade.catalog.domain.model.valueobject.ProductCover;
import cn.dextea.trade.catalog.domain.model.valueobject.ProductStoreStatus;

import java.util.List;
import java.util.Map;

public interface CatalogGateway {
    List<Product> findProducts(List<Long> ids);

    List<ProductStoreStatus> findProductStoreStatus(List<Long> productIds, Long storeId);

    Map<Long, ProductCover> findProductCovers(List<Long> productIds);

    Map<Long, String> findCoverUrls(List<Long> coverIds);

    List<Customization> findCustomizations(List<Long> ids);

    List<CustomizationOption> findOptions(List<Long> ids);

    List<CustomizationOptionStoreStatus> findOptionStoreStatus(List<Long> optionIds, Long storeId);

    Store findStore(Long id);

    Customer findCustomer(Long id);
}
