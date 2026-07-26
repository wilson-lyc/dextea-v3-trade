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
 * 商品目录只读仓储实现：聚合各类 MyBatis Mapper，承接领域层 {@link CatalogRepository} 端口。
 */
@Repository
@RequiredArgsConstructor
public class CatalogRepositoryImpl implements CatalogRepository {

    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final GalleryMapper galleryMapper;
    private final ProductStoreStatusMapper productStoreStatusMapper;
    private final CustomizationMapper customizationMapper;
    private final CustomizationOptionMapper customizationOptionMapper;
    private final CustomizationOptionStoreStatusMapper customizationOptionStoreStatusMapper;
    private final StoreMapper storeMapper;
    private final CustomerMapper customerMapper;

    @Override
    public List<Product> findProductsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return productMapper.selectByIds(ids);
    }

    @Override
    public List<ProductImage> findCoverImagesByProductIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return productImageMapper.selectCoverImagesByProductIds(productIds);
    }

    @Override
    public List<Gallery> findGalleriesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return galleryMapper.selectByIds(ids);
    }

    @Override
    public List<ProductStoreStatus> findProductStoreStatus(List<Long> productIds, Long storeId) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return productStoreStatusMapper.selectByProductIdsAndStoreId(productIds, storeId);
    }

    @Override
    public List<Customization> findCustomizationsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return customizationMapper.selectByIds(ids);
    }

    @Override
    public List<CustomizationOption> findCustomizationOptionsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return customizationOptionMapper.selectByIds(ids);
    }

    @Override
    public List<CustomizationOptionStoreStatus> findOptionStoreStatus(List<Long> optionIds, Long storeId) {
        if (optionIds == null || optionIds.isEmpty()) {
            return List.of();
        }
        return customizationOptionStoreStatusMapper.selectByOptionIdsAndStoreId(optionIds, storeId);
    }

    @Override
    public Store findStoreById(Long id) {
        return storeMapper.selectById(id);
    }

    @Override
    public Customer findCustomerById(Long id) {
        return customerMapper.selectById(id);
    }
}
