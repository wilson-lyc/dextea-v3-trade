package cn.dextea.trade.catalog.infrastructure.persistence;

import cn.dextea.trade.catalog.domain.model.Customization;
import cn.dextea.trade.catalog.domain.model.CustomizationOption;
import cn.dextea.trade.catalog.domain.model.CustomizationOptionStoreStatus;
import cn.dextea.trade.catalog.domain.model.Customer;
import cn.dextea.trade.catalog.domain.model.Gallery;
import cn.dextea.trade.catalog.domain.model.Product;
import cn.dextea.trade.catalog.domain.model.ProductImage;
import cn.dextea.trade.catalog.domain.model.ProductStoreStatus;
import cn.dextea.trade.catalog.domain.model.Store;
import cn.dextea.trade.catalog.domain.repository.CatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品目录只读仓储适配：聚合单一 MyBatis Mapper，实现领域层 {@link CatalogRepository} 端口。
 * 空集合守卫集中在此处（唯一一层），避免重复判断。
 */
@Repository
@RequiredArgsConstructor
public class CatalogPersistenceAdapter implements CatalogRepository {

    private final CatalogMapper catalogMapper;

    @Override
    public List<Product> findProductsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return catalogMapper.selectProductsByIds(ids);
    }

    @Override
    public List<ProductImage> findCoverImagesByProductIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return catalogMapper.selectCoverImagesByProductIds(productIds);
    }

    @Override
    public List<Gallery> findGalleriesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return catalogMapper.selectGalleriesByIds(ids);
    }

    @Override
    public List<ProductStoreStatus> findProductStoreStatus(List<Long> productIds, Long storeId) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return catalogMapper.selectProductStoreStatusByProductIdsAndStoreId(productIds, storeId);
    }

    @Override
    public List<Customization> findCustomizationsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return catalogMapper.selectCustomizationsByIds(ids);
    }

    @Override
    public List<CustomizationOption> findCustomizationOptionsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return catalogMapper.selectCustomizationOptionsByIds(ids);
    }

    @Override
    public List<CustomizationOptionStoreStatus> findOptionStoreStatus(List<Long> optionIds, Long storeId) {
        if (optionIds == null || optionIds.isEmpty()) {
            return List.of();
        }
        return catalogMapper.selectOptionStoreStatusByOptionIdsAndStoreId(optionIds, storeId);
    }

    @Override
    public Store findStoreById(Long id) {
        return catalogMapper.selectStoreById(id);
    }

    @Override
    public Customer findCustomerById(Long id) {
        return catalogMapper.selectCustomerById(id);
    }
}
