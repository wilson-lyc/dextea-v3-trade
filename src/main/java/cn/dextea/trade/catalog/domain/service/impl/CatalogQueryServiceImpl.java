package cn.dextea.trade.catalog.domain.service.impl;

import cn.dextea.trade.catalog.domain.model.Customization;
import cn.dextea.trade.catalog.domain.model.CustomizationOption;
import cn.dextea.trade.catalog.domain.model.CustomizationOptionStoreStatus;
import cn.dextea.trade.catalog.domain.model.Customer;
import cn.dextea.trade.catalog.domain.model.Gallery;
import cn.dextea.trade.catalog.domain.model.Product;
import cn.dextea.trade.catalog.domain.model.ProductImage;
import cn.dextea.trade.catalog.domain.model.ProductStoreStatus;
import cn.dextea.trade.catalog.domain.model.Store;
import cn.dextea.trade.catalog.domain.service.CatalogQueryService;
import cn.dextea.trade.catalog.infrastructure.persistence.CustomizationMapper;
import cn.dextea.trade.catalog.infrastructure.persistence.CustomizationOptionMapper;
import cn.dextea.trade.catalog.infrastructure.persistence.CustomizationOptionStoreStatusMapper;
import cn.dextea.trade.catalog.infrastructure.persistence.CustomerMapper;
import cn.dextea.trade.catalog.infrastructure.persistence.GalleryMapper;
import cn.dextea.trade.catalog.infrastructure.persistence.ProductImageMapper;
import cn.dextea.trade.catalog.infrastructure.persistence.ProductMapper;
import cn.dextea.trade.catalog.infrastructure.persistence.ProductStoreStatusMapper;
import cn.dextea.trade.catalog.infrastructure.persistence.StoreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogQueryServiceImpl implements CatalogQueryService {

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
