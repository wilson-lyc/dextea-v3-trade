package cn.dextea.trade.catalog.infrastructure.gateway;

import cn.dextea.trade.catalog.domain.model.aggregate.Customer;
import cn.dextea.trade.catalog.domain.model.aggregate.Product;
import cn.dextea.trade.catalog.domain.model.aggregate.Store;
import cn.dextea.trade.catalog.domain.model.valueobject.Customization;
import cn.dextea.trade.catalog.domain.model.valueobject.CustomizationOption;
import cn.dextea.trade.catalog.domain.model.valueobject.CustomizationOptionStoreStatus;
import cn.dextea.trade.catalog.domain.model.valueobject.ProductCover;
import cn.dextea.trade.catalog.domain.model.valueobject.ProductStoreStatus;
import cn.dextea.trade.catalog.infrastructure.mapper.CatalogMapper;
import cn.dextea.trade.catalog.infrastructure.po.GalleryPO;
import cn.dextea.trade.catalog.infrastructure.po.ProductImagePO;
import cn.dextea.trade.catalog.infrastructure.translator.CustomizationTranslator;
import cn.dextea.trade.catalog.infrastructure.translator.CustomerTranslator;
import cn.dextea.trade.catalog.infrastructure.translator.ProductTranslator;
import cn.dextea.trade.catalog.infrastructure.translator.StoreTranslator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CatalogGatewayImpl implements CatalogGateway {
    private final CatalogMapper catalogMapper;

    @Override
    public List<Product> findProducts(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ProductTranslator.toProducts(catalogMapper.selectProductsByIds(ids));
    }

    @Override
    public List<ProductStoreStatus> findProductStoreStatus(List<Long> productIds, Long storeId) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return ProductTranslator.toProductStoreStatusList(
                catalogMapper.selectProductStoreStatusByProductIdsAndStoreId(productIds, storeId));
    }

    @Override
    public Map<Long, ProductCover> findProductCovers(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        List<ProductImagePO> images = catalogMapper.selectCoverImagesByProductIds(productIds);
        List<Long> imageIds = ProductTranslator.extractImageIds(images);
        List<GalleryPO> galleries = imageIds.isEmpty() ? List.of() : catalogMapper.selectGalleriesByIds(imageIds);
        return ProductTranslator.toProductCovers(images, galleries);
    }

    @Override
    public Map<Long, String> findCoverUrls(List<Long> coverIds) {
        if (coverIds == null || coverIds.isEmpty()) {
            return Map.of();
        }
        return ProductTranslator.toCoverUrls(catalogMapper.selectGalleriesByIds(coverIds));
    }

    @Override
    public List<Customization> findCustomizations(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return CustomizationTranslator.toCustomizations(catalogMapper.selectCustomizationsByIds(ids));
    }

    @Override
    public List<CustomizationOption> findOptions(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return CustomizationTranslator.toOptions(catalogMapper.selectCustomizationOptionsByIds(ids));
    }

    @Override
    public List<CustomizationOptionStoreStatus> findOptionStoreStatus(List<Long> optionIds, Long storeId) {
        if (optionIds == null || optionIds.isEmpty()) {
            return List.of();
        }
        return CustomizationTranslator.toOptionStoreStatusList(
                catalogMapper.selectOptionStoreStatusByOptionIdsAndStoreId(optionIds, storeId));
    }

    @Override
    public Store findStore(Long id) {
        if (id == null) {
            return null;
        }
        return StoreTranslator.toStore(catalogMapper.selectStoreById(id));
    }

    @Override
    public Customer findCustomer(Long id) {
        if (id == null) {
            return null;
        }
        return CustomerTranslator.toCustomer(catalogMapper.selectCustomerById(id));
    }
}
