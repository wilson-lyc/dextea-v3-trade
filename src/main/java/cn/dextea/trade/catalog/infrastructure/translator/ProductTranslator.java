package cn.dextea.trade.catalog.infrastructure.translator;

import cn.dextea.trade.catalog.domain.model.aggregate.Product;
import cn.dextea.trade.catalog.domain.model.valueobject.ProductCover;
import cn.dextea.trade.catalog.domain.model.valueobject.ProductStoreStatus;
import cn.dextea.trade.catalog.infrastructure.po.GalleryPO;
import cn.dextea.trade.catalog.infrastructure.po.ProductImagePO;
import cn.dextea.trade.catalog.infrastructure.po.ProductPO;
import cn.dextea.trade.catalog.infrastructure.po.ProductStoreStatusPO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ProductTranslator {
    private ProductTranslator() {
    }

    public static Product toProduct(ProductPO po) {
        if (po == null) {
            return null;
        }
        return Product.builder()
                .id(po.getId())
                .name(po.getName())
                .status(po.getStatus())
                .price(po.getPrice())
                .build();
    }

    public static List<Product> toProducts(List<ProductPO> pos) {
        return pos == null ? List.of() : pos.stream().map(ProductTranslator::toProduct).collect(Collectors.toList());
    }

    public static ProductStoreStatus toProductStoreStatus(ProductStoreStatusPO po) {
        if (po == null) {
            return null;
        }
        return ProductStoreStatus.builder()
                .productId(po.getProductId())
                .storeId(po.getStoreId())
                .status(po.getStatus())
                .build();
    }

    public static List<ProductStoreStatus> toProductStoreStatusList(List<ProductStoreStatusPO> pos) {
        return pos == null ? List.of()
                : pos.stream().map(ProductTranslator::toProductStoreStatus).collect(Collectors.toList());
    }

    public static Map<Long, ProductCover> toProductCovers(List<ProductImagePO> images, List<GalleryPO> galleries) {
        Map<Long, ProductCover> result = new LinkedHashMap<>();
        if (images == null || images.isEmpty()) {
            return result;
        }
        Map<Long, String> urlMap = toCoverUrls(galleries);
        Map<Long, Long> coverIdMap = new LinkedHashMap<>();
        images.forEach(pi -> coverIdMap.putIfAbsent(pi.getProductId(), pi.getImageId()));
        coverIdMap.forEach((productId, imageId) ->
                result.put(productId, new ProductCover(productId, imageId, urlMap.get(imageId))));
        return result;
    }

    public static Map<Long, String> toCoverUrls(List<GalleryPO> galleries) {
        Map<Long, String> map = new HashMap<>();
        if (galleries == null) {
            return map;
        }
        galleries.forEach(g -> {
            if (g.getId() != null && g.getUrl() != null) {
                map.putIfAbsent(g.getId(), g.getUrl());
            }
        });
        return map;
    }

    public static List<Long> extractImageIds(List<ProductImagePO> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(images.stream()
                .map(ProductImagePO::getImageId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new)));
    }
}
