package cn.dextea.trade.order.infrastructure.persistence.repository;

import cn.dextea.trade.order.domain.model.aggregate.Product;
import cn.dextea.trade.order.domain.model.valueobject.ProductCover;
import cn.dextea.trade.order.domain.model.valueobject.ProductStoreStatus;
import cn.dextea.trade.order.domain.port.ProductRepository;
import cn.dextea.trade.order.infrastructure.persistence.mapper.ProductMapper;
import cn.dextea.trade.order.infrastructure.persistence.po.GalleryPO;
import cn.dextea.trade.order.infrastructure.persistence.po.ProductImagePO;
import cn.dextea.trade.order.infrastructure.persistence.translator.ProductTranslator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductMapper productMapper;

    @Override
    public List<Product> findProducts(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ProductTranslator.toProducts(productMapper.selectProductsByIds(ids));
    }

    @Override
    public List<ProductStoreStatus> findProductStoreStatus(List<Long> productIds, Long storeId) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return ProductTranslator.toProductStoreStatusList(
                productMapper.selectProductStoreStatusByProductIdsAndStoreId(productIds, storeId));
    }

    @Override
    public Map<Long, ProductCover> findProductCovers(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        List<ProductImagePO> images = productMapper.selectCoverImagesByProductIds(productIds);
        List<Long> imageIds = ProductTranslator.extractImageIds(images);
        List<GalleryPO> galleries = imageIds.isEmpty() ? List.of() : productMapper.selectGalleriesByIds(imageIds);
        return ProductTranslator.toProductCovers(images, galleries);
    }

    @Override
    public Map<Long, String> findCoverUrls(List<Long> coverIds) {
        if (coverIds == null || coverIds.isEmpty()) {
            return Map.of();
        }
        return ProductTranslator.toCoverUrls(productMapper.selectGalleriesByIds(coverIds));
    }
}
