package cn.dextea.trade.order.application.loader;

import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.aggregate.Product;
import cn.dextea.trade.order.domain.model.valueobject.CatalogSnapshot;
import cn.dextea.trade.order.domain.model.valueobject.Customization;
import cn.dextea.trade.order.domain.model.valueobject.CustomizationOption;
import cn.dextea.trade.order.domain.model.valueobject.CustomizationOptionStoreStatus;
import cn.dextea.trade.order.domain.model.valueobject.ProductCover;
import cn.dextea.trade.order.domain.model.valueobject.ProductStoreStatus;
import cn.dextea.trade.order.domain.model.valueobject.SkuSelection;
import cn.dextea.trade.order.domain.port.CustomerRepository;
import cn.dextea.trade.order.domain.port.CustomizationRepository;
import cn.dextea.trade.order.domain.port.ProductRepository;
import cn.dextea.trade.order.domain.port.StoreRepository;
import cn.dextea.trade.shared.domain.error.BizError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CatalogSnapshotLoader {

    private final ProductRepository productRepository;
    private final CustomizationRepository customizationRepository;
    private final StoreRepository storeRepository;
    private final CustomerRepository customerRepository;

    public CatalogSnapshot load(Long storeId, Long customerId, List<SkuSelection> selections) {
        Set<Long> productIds = new LinkedHashSet<>();
        Set<Long> customizationIds = new LinkedHashSet<>();
        Set<Long> optionIds = new LinkedHashSet<>();
        for (SkuSelection selection : selections) {
            productIds.add(selection.getProductId());
            customizationIds.addAll(selection.getCustomizationIds());
            optionIds.addAll(selection.getOptionIds());
        }

        return CatalogSnapshot.builder()
                .store(storeRepository.findStore(storeId))
                .customer(customerRepository.findCustomer(customerId))
                .products(loadProducts(productIds))
                .productCovers(loadProductCovers(productIds))
                .productStoreStatuses(loadProductStoreStatuses(productIds, storeId))
                .customizations(loadCustomizations(customizationIds))
                .options(loadOptions(optionIds))
                .optionStoreStatuses(loadOptionStoreStatuses(optionIds, storeId))
                .build();
    }

    private Map<Long, Product> loadProducts(Set<Long> productIds) {
        return requireAllFound(productIds, productRepository::findProducts, Product::getId,
                OrderErrorCode.PRODUCT_ID_INVALID, "商品");
    }

    private Map<Long, ProductCover> loadProductCovers(Set<Long> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }
        return productRepository.findProductCovers(new ArrayList<>(productIds));
    }

    private Map<Long, ProductStoreStatus> loadProductStoreStatuses(Set<Long> productIds, Long storeId) {
        if (productIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, ProductStoreStatus> map = new HashMap<>();
        productRepository.findProductStoreStatus(new ArrayList<>(productIds), storeId)
                .forEach(status -> map.put(status.getProductId(), status));
        return map;
    }

    private Map<Long, Customization> loadCustomizations(Set<Long> customizationIds) {
        return requireAllFound(customizationIds, customizationRepository::findCustomizations, Customization::getId,
                OrderErrorCode.CUSTOMIZATION_ID_INVALID, "客制化项目");
    }

    private Map<Long, CustomizationOption> loadOptions(Set<Long> optionIds) {
        return requireAllFound(optionIds, customizationRepository::findOptions, CustomizationOption::getId,
                OrderErrorCode.CUSTOMIZATION_OPTION_ID_INVALID, "客制化选项");
    }

    private Map<Long, CustomizationOptionStoreStatus> loadOptionStoreStatuses(Set<Long> optionIds, Long storeId) {
        if (optionIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, CustomizationOptionStoreStatus> map = new HashMap<>();
        customizationRepository.findOptionStoreStatus(new ArrayList<>(optionIds), storeId)
                .forEach(status -> map.put(status.getCustomizationOptionId(), status));
        return map;
    }

    private <T> Map<Long, T> requireAllFound(
            Set<Long> ids,
            Function<List<Long>, List<T>> batchLoader,
            Function<T, Long> idExtractor,
            OrderErrorCode errorCode,
            String entityName) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, T> map = batchLoader.apply(new ArrayList<>(ids)).stream()
                .collect(Collectors.toMap(idExtractor, e -> e, (a, b) -> a));
        List<Long> notFound = ids.stream().filter(id -> !map.containsKey(id)).toList();
        if (!notFound.isEmpty()) {
            throw new BizError(errorCode, entityName + "ID非法: " + notFound.stream()
                    .map(String::valueOf).collect(Collectors.joining("、")));
        }
        return map;
    }
}
