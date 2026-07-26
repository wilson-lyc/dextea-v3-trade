package cn.dextea.trade.catalog.domain.service;

import cn.dextea.trade.catalog.domain.model.Customization;
import cn.dextea.trade.catalog.domain.model.CustomizationOption;
import cn.dextea.trade.catalog.domain.model.CustomizationOptionStoreStatus;
import cn.dextea.trade.catalog.domain.model.Customer;
import cn.dextea.trade.catalog.domain.model.Gallery;
import cn.dextea.trade.catalog.domain.model.Product;
import cn.dextea.trade.catalog.domain.model.ProductImage;
import cn.dextea.trade.catalog.domain.model.ProductStoreStatus;
import cn.dextea.trade.catalog.domain.model.Store;

import java.util.List;

public interface CatalogQueryService {

    List<Product> findProductsByIds(List<Long> ids);

    List<ProductImage> findCoverImagesByProductIds(List<Long> productIds);

    List<Gallery> findGalleriesByIds(List<Long> ids);

    List<ProductStoreStatus> findProductStoreStatus(List<Long> productIds, Long storeId);

    List<Customization> findCustomizationsByIds(List<Long> ids);

    List<CustomizationOption> findCustomizationOptionsByIds(List<Long> ids);

    List<CustomizationOptionStoreStatus> findOptionStoreStatus(List<Long> optionIds, Long storeId);

    Store findStoreById(Long id);

    Customer findCustomerById(Long id);
}
