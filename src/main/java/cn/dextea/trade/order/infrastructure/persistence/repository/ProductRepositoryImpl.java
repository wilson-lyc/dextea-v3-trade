package cn.dextea.trade.order.infrastructure.persistence.repository;

import cn.dextea.trade.order.domain.exception.ProductNotFoundException;
import cn.dextea.trade.order.domain.model.Product;
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
                throw new ProductNotFoundException(id);
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

        Map<Long, ProductStoreStatusPO> productStoreStatusByProductId = productIds.stream()
                .map(pid -> productStoreStatusMapper.selectByProductIdAndStoreId(pid, storeId))
                .filter(po -> po != null)
                .collect(Collectors.toMap(ProductStoreStatusPO::getProductId, Function.identity()));

        return productPOs.stream().collect(Collectors.toMap(
                ProductsPO::getId,
                po -> {
                    List<CustomizationItem> items = itemsByProductId
                            .getOrDefault(po.getId(), Collections.emptyList())
                            .stream()
                            .map(itemPO -> toItemDomain(itemPO, optionsByItemId, optionStoreStatusByOptionId))
                            .collect(Collectors.toList());
                    ProductCover cover = resolveCover(po.getId());
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

    private ProductCover resolveCover(Long productId) {
        ProductImagePO imagePO = productImageMapper.selectCoverByProductId(productId);
        if (imagePO == null) {
            return null;
        }
        GalleryPO galleryPO = galleryMapper.selectById(imagePO.getImageId());
        if (galleryPO == null) {
            return null;
        }
        return ProductCover.builder()
                .id(galleryPO.getId())
                .url(galleryPO.getUrl())
                .build();
    }
}
