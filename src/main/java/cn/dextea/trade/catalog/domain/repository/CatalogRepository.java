package cn.dextea.trade.catalog.domain.repository;

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

/**
 * 商品目录只读仓储端口：对外暴露批量查询参考数据的契约。
 * 由基础设施层 {@link cn.dextea.trade.catalog.infrastructure.persistence.CatalogPersistenceAdapter}
 * 实现，保持领域层不依赖持久化细节。
 */
public interface CatalogRepository {

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
