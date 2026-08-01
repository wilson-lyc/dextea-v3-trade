package cn.dextea.trade.order.infrastructure.persistence.repository;

import cn.dextea.trade.order.domain.model.CustomizationItem;
import cn.dextea.trade.order.domain.model.CustomizationOption;
import cn.dextea.trade.order.domain.model.aggregate.Product;
import cn.dextea.trade.order.domain.model.enums.CustomizationItemStatus;
import cn.dextea.trade.order.domain.model.enums.CustomizationOptionGlobalStatus;
import cn.dextea.trade.order.domain.model.enums.CustomizationOptionStoreStatus;
import cn.dextea.trade.order.domain.model.enums.ProductGlobalStatus;
import cn.dextea.trade.order.domain.model.enums.ProductStoreStatus;
import cn.dextea.trade.order.domain.repository.ProductRepository;
import cn.dextea.trade.order.infrastructure.persistence.mapper.CustomizationItemMapper;
import cn.dextea.trade.order.infrastructure.persistence.mapper.CustomizationOptionMapper;
import cn.dextea.trade.order.infrastructure.persistence.mapper.ProductImageMapper;
import cn.dextea.trade.order.infrastructure.persistence.mapper.ProductMapper;
import cn.dextea.trade.order.infrastructure.persistence.mapper.StoreStatusMapper;
import cn.dextea.trade.order.infrastructure.persistence.po.CustomizationItemPO;
import cn.dextea.trade.order.infrastructure.persistence.po.CustomizationOptionPO;
import cn.dextea.trade.order.infrastructure.persistence.po.CustomizationOptionStoreStatusPO;
import cn.dextea.trade.order.domain.model.enums.CustomizationItemStatus;
import cn.dextea.trade.order.domain.model.enums.CustomizationOptionGlobalStatus;
import cn.dextea.trade.order.domain.model.enums.CustomizationOptionStoreStatus;
import cn.dextea.trade.order.domain.model.enums.ProductGlobalStatus;
import cn.dextea.trade.order.domain.model.enums.ProductStoreStatus;
import cn.dextea.trade.order.infrastructure.persistence.po.ProductCoverPO;
import cn.dextea.trade.order.infrastructure.persistence.po.ProductPO;
import cn.dextea.trade.order.infrastructure.persistence.po.ProductStoreStatusPO;
import cn.dextea.trade.shared.domain.enumeration.CodeEnum;
import cn.dextea.trade.shared.domain.enumeration.EnumUtils;
import cn.dextea.trade.shared.domain.money.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private static final int DEFAULT_DISABLED_STATUS = 0;

    private final ProductMapper productMapper;
    private final CustomizationItemMapper customizationItemMapper;
    private final CustomizationOptionMapper customizationOptionMapper;
    private final StoreStatusMapper storeStatusMapper;
    private final ProductImageMapper productImageMapper;

    @Override
    public Map<Long, Product> getProductByIdsWithStoreId(Set<Long> ids, Long storeId) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        List<ProductPO> products = productMapper.selectByIds(new ArrayList<>(ids));
        if (products.isEmpty()) {
            return Map.of();
        }
        List<Long> productIds = products.stream().map(ProductPO::getId).collect(Collectors.toList());

        Map<Long, Integer> productStoreStatusMap = storeStatusMapper
                .selectProductStoreStatus(productIds, storeId)
                .stream()
                .collect(Collectors.toMap(ProductStoreStatusPO::getProductId, ProductStoreStatusPO::getStatus, (a, b) -> a));

        Map<Long, List<CustomizationItem>> customizationsByProduct = loadCustomizations(productIds, storeId);

        List<ProductCoverPO> covers = productImageMapper.selectCoversByProductIds(productIds);
        Map<Long, ProductCoverPO> coverByProduct = covers.stream()
                .collect(Collectors.toMap(ProductCoverPO::getProductId, c -> c, (a, b) -> a));

        Map<Long, Product> result = new LinkedHashMap<>();
        for (ProductPO po : products) {
            Long productId = po.getId();
            Integer storeStatusCode = productStoreStatusMap.getOrDefault(productId, DEFAULT_DISABLED_STATUS);
            ProductCoverPO cover = coverByProduct.get(productId);
            ProductCover productCover = cover != null
                    ? ProductCover.builder().id(cover.getImageId()).url(cover.getUrl()).build()
                    : null;
            result.put(productId, Product.builder()
                    .id(productId)
                    .name(po.getName())
                    .globalStatus(resolve(ProductGlobalStatus.class, po.getStatus()))
                    .storeStatus(resolve(ProductStoreStatus.class, storeStatusCode))
                    .price(Money.of(po.getPrice()))
                    .cover(productCover)
                    .customization(customizationsByProduct.getOrDefault(productId, List.of()))
                    .build());
        }
        return result;
    }

    private Map<Long, List<CustomizationItem>> loadCustomizations(List<Long> productIds, Long storeId) {
        List<CustomizationItemPO> itemPOs = customizationItemMapper.selectByProductIds(productIds);
        if (itemPOs.isEmpty()) {
            return Map.of();
        }
        List<Long> itemIds = itemPOs.stream().map(CustomizationItemPO::getId).collect(Collectors.toList());
        List<CustomizationOptionPO> optionPOs = customizationOptionMapper.selectByItemIds(itemIds);
        Map<Long, List<CustomizationOptionPO>> optionsByItem = optionPOs.stream()
                .collect(Collectors.groupingBy(CustomizationOptionPO::getItemId));

        List<Long> optionIds = optionPOs.stream().map(CustomizationOptionPO::getId).collect(Collectors.toList());
        Map<Long, Integer> optionStoreStatusMap = optionIds.isEmpty() ? Map.of()
                : storeStatusMapper.selectCustomizationOptionStoreStatus(optionIds, storeId).stream()
                .collect(Collectors.toMap(CustomizationOptionStoreStatusPO::getOptionId,
                        CustomizationOptionStoreStatusPO::getStatus, (a, b) -> a));

        Map<Long, List<CustomizationItem>> result = new LinkedHashMap<>();
        for (CustomizationItemPO itemPO : itemPOs) {
            List<CustomizationOptionPO> itemOptions = optionsByItem.getOrDefault(itemPO.getId(), List.of());
            List<CustomizationOption> options = itemOptions.stream()
                    .map(op -> toCustomizationOption(op, optionStoreStatusMap.getOrDefault(op.getId(), DEFAULT_DISABLED_STATUS)))
                    .collect(Collectors.toList());
            result.computeIfAbsent(itemPO.getProductId(), k -> new ArrayList<>())
                    .add(CustomizationItem.builder()
                            .id(itemPO.getId())
                            .name(itemPO.getName())
                            .status(resolve(CustomizationItemStatus.class, itemPO.getStatus()))
                            .options(options)
                            .build());
        }
        return result;
    }

    private CustomizationOption toCustomizationOption(CustomizationOptionPO po, int storeStatusCode) {
        return CustomizationOption.builder()
                .id(po.getId())
                .name(po.getName())
                .price(Money.of(po.getPrice()))
                .globalStatus(resolve(CustomizationOptionGlobalStatus.class, po.getStatus()))
                .storeStatus(resolve(CustomizationOptionStoreStatus.class, storeStatusCode))
                .build();
    }

    private static <E extends Enum<E> & CodeEnum> E resolve(Class<E> type, Integer code) {
        if (code == null) {
            return type.getEnumConstants()[0];
        }
        return EnumUtils.of(type, code);
    }

}
