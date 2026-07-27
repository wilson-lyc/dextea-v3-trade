package cn.dextea.trade.order.infrastructure.gateway.translator;

import cn.dextea.trade.order.domain.gateway.ProductCover;
import cn.dextea.trade.order.domain.model.valueobject.Product;
import cn.dextea.trade.order.domain.model.valueobject.ProductStoreStatus;
import cn.dextea.trade.order.infrastructure.gateway.po.GalleryPO;
import cn.dextea.trade.order.infrastructure.gateway.po.ProductImagePO;
import cn.dextea.trade.order.infrastructure.gateway.po.ProductPO;
import cn.dextea.trade.order.infrastructure.gateway.po.ProductStoreStatusPO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品 PO → 领域值对象清洗器。
 *
 * <p>图库两段 join（product_images → gallery）在此完成，领域层只见
 * {@link ProductCover}（productId → 封面标识 + 封面 URL），不感知图库表结构。</p>
 */
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

    /**
     * 清洗封面映射：product_images 已按 sort/created_at 排序，每个商品取第一张作为封面，
     * 再用 gallery 补齐 URL，产出 productId → {@link ProductCover}。
     */
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

    /** gallery 清洗为 coverId → url。 */
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

    /** 提取封面图片 ID 集合（保持出现顺序、去重）。 */
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
