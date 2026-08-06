package cn.dextea.trade.order.infrastructure.persistence.repository;

import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.Product;
import cn.dextea.trade.shared.domain.error.BizError;
import cn.dextea.trade.order.domain.model.ProductCover;
import cn.dextea.trade.order.domain.model.CustomizationItem;
import cn.dextea.trade.order.domain.model.CustomizationOption;
import cn.dextea.trade.order.domain.repository.ProductRepository;
import cn.dextea.trade.order.infrastructure.persistence.converter.ProductConverter;
import cn.dextea.trade.order.infrastructure.persistence.converter.CustomizationItemConverter;
import cn.dextea.trade.order.infrastructure.persistence.converter.CustomizationOptionConverter;
import cn.dextea.trade.order.infrastructure.persistence.mapper.ProductMapper;
import cn.dextea.trade.order.infrastructure.persistence.mapper.CustomizationItemMapper;
import cn.dextea.trade.order.infrastructure.persistence.mapper.CustomizationOptionMapper;
import cn.dextea.trade.order.infrastructure.persistence.mapper.ProductStoreStatusMapper;
import cn.dextea.trade.order.infrastructure.persistence.mapper.CustomizationOptionStoreStatusMapper;
import cn.dextea.trade.order.infrastructure.persistence.mapper.ProductImageMapper;
import cn.dextea.trade.order.infrastructure.persistence.mapper.GalleryMapper;
import cn.dextea.trade.order.infrastructure.persistence.po.ProductsPO;
import cn.dextea.trade.order.infrastructure.persistence.po.CustomizationItemPO;
import cn.dextea.trade.order.infrastructure.persistence.po.CustomizationOptionPO;
import cn.dextea.trade.order.infrastructure.persistence.po.ProductStoreStatusPO;
import cn.dextea.trade.order.infrastructure.persistence.po.CustomizationOptionStoreStatusPO;
import cn.dextea.trade.order.infrastructure.persistence.po.ProductImagePO;
import cn.dextea.trade.order.infrastructure.persistence.po.GalleryPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductMapper productMapper;
    private final CustomizationItemMapper customizationItemMapper;
    private final CustomizationOptionMapper customizationOptionMapper;
    private final ProductStoreStatusMapper productStoreStatusMapper;
    private final CustomizationOptionStoreStatusMapper customizationOptionStoreStatusMapper;
    private final ProductImageMapper productImageMapper;
    private final GalleryMapper galleryMapper;
    private final ProductConverter productConverter;
    private final CustomizationItemConverter customizationItemConverter;
    private final CustomizationOptionConverter customizationOptionConverter;

    @Override
    public Map<Long, Product> getProductByIdsWithStoreId(Set<Long> ids, Long storeId) {
        List<ProductsPO> productPOs = productMapper.selectByIds(ids);
        Set<Long> productIds = productPOs.stream().map(ProductsPO::getId).collect(Collectors.toSet());
        for (Long id : ids) {
            if (!productIds.contains(id)) {
                throw new BizError(OrderErrorCode.PRODUCT_NOT_FOUND);
            }
        }

        List<CustomizationItemPO> itemPOs = customizationItemMapper.selectByProductIds(productIds);
        Set<Long> itemIds = itemPOs.stream().map(CustomizationItemPO::getId).collect(Collectors.toSet());

        List<CustomizationOptionPO> optionPOs = customizationOptionMapper.selectByItemIds(itemIds);

        Set<Long> optionIds = optionPOs.stream().map(CustomizationOptionPO::getId).collect(Collectors.toSet());
        List<CustomizationOptionStoreStatusPO> optionStoreStatusPOs =
                optionIds.isEmpty()
                        ? Collections.emptyList()
                        : customizationOptionStoreStatusMapper.selectByOptionIdsAndStoreId(optionIds, storeId);

        Map<Long, CustomizationOptionStoreStatusPO> optionStoreStatusByOptionId = optionStoreStatusPOs.stream()
                .collect(Collectors.toMap(CustomizationOptionStoreStatusPO::getOptionId, Function.identity()));

        Map<Long, List<CustomizationOptionPO>> optionsByItemId = optionPOs.stream()
                .collect(Collectors.groupingBy(CustomizationOptionPO::getItemId));

        Map<Long, List<CustomizationItemPO>> itemsByProductId = itemPOs.stream()
                .collect(Collectors.groupingBy(CustomizationItemPO::getProductId));

        List<ProductStoreStatusPO> productStoreStatusPOs =
                productStoreStatusMapper.selectByProductIdsAndStoreId(productIds, storeId);
        Map<Long, ProductStoreStatusPO> productStoreStatusByProductId = productStoreStatusPOs.stream()
                .collect(Collectors.toMap(ProductStoreStatusPO::getProductId, Function.identity()));

        Map<Long, ProductCover> coverByProductId = resolveCovers(productIds);

        return productPOs.stream().collect(Collectors.toMap(
                ProductsPO::getId,
                po -> {
                    List<CustomizationItem> items = itemsByProductId
                            .getOrDefault(po.getId(), Collections.emptyList())
                            .stream()
                            .map(itemPO -> toItemDomain(itemPO, optionsByItemId, optionStoreStatusByOptionId))
                            .collect(Collectors.toList());
                    ProductCover cover = coverByProductId.get(po.getId());
                    ProductStoreStatusPO storeStatusPO = productStoreStatusByProductId.get(po.getId());
                    Product product = productConverter.toDomain(po, storeStatusPO, cover);
                    product.setCustomization(items);
                    return product;
                },
                (a, b) -> a));
    }

    private CustomizationItem toItemDomain(CustomizationItemPO itemPO,
                                            Map<Long, List<CustomizationOptionPO>> optionsByItemId,
                                            Map<Long, CustomizationOptionStoreStatusPO> optionStoreStatusByOptionId) {
        List<CustomizationOptionPO> itemOptions =
                optionsByItemId.getOrDefault(itemPO.getId(), Collections.emptyList());
        List<CustomizationOption> options = itemOptions.stream()
                .map(op -> {
                    CustomizationOptionStoreStatusPO osStatus = optionStoreStatusByOptionId.get(op.getId());
                    return customizationOptionConverter.toDomain(op, osStatus);
                })
                .collect(Collectors.toList());
        return customizationItemConverter.toDomain(itemPO, options);
    }

    private Map<Long, ProductCover> resolveCovers(Set<Long> productIds) {
        if (productIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ProductImagePO> imagePOs = productImageMapper.selectCoversByProductIds(productIds);
        Map<Long, Long> imageIdByProductId = imagePOs.stream()
                .filter(po -> po.getImageId() != null)
                .collect(Collectors.toMap(ProductImagePO::getProductId, ProductImagePO::getImageId, (a, b) -> a));
        if (imageIdByProductId.isEmpty()) {
            return Collections.emptyMap();
        }
        List<GalleryPO> galleryPOs = galleryMapper.selectByIds(imageIdByProductId.values());
        Map<Long, String> urlById = galleryPOs.stream()
                .collect(Collectors.toMap(GalleryPO::getId, GalleryPO::getUrl));
        Map<Long, ProductCover> result = new java.util.HashMap<>();
        imageIdByProductId.forEach((productId, imageId) -> {
            String url = urlById.get(imageId);
            if (url != null) {
                result.put(productId, ProductCover.builder().id(imageId).url(url).build());
            }
        });
        return result;
    }
}
