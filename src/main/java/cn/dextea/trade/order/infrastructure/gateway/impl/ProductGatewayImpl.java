package cn.dextea.trade.order.infrastructure.gateway.impl;

import cn.dextea.trade.order.domain.gateway.ProductCover;
import cn.dextea.trade.order.domain.gateway.ProductGateway;
import cn.dextea.trade.order.domain.model.valueobject.Product;
import cn.dextea.trade.order.domain.model.valueobject.ProductStoreStatus;
import cn.dextea.trade.order.infrastructure.gateway.mapper.CatalogMapper;
import cn.dextea.trade.order.infrastructure.gateway.po.GalleryPO;
import cn.dextea.trade.order.infrastructure.gateway.po.ProductImagePO;
import cn.dextea.trade.order.infrastructure.gateway.translator.ProductTranslator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 商品网关实现（ACL）：查询外部商品表并清洗为领域值对象。
 *
 * <p>封面查询在此完成 product_images → gallery 两段 join，
 * 领域层只消费 productId → 封面标识/URL 的清洗结果。</p>
 */
@Component
@RequiredArgsConstructor
public class ProductGatewayImpl implements ProductGateway {

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
}
