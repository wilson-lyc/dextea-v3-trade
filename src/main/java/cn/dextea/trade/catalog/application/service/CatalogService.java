package cn.dextea.trade.catalog.application.service;

import cn.dextea.trade.catalog.domain.exception.CatalogErrorCode;
import cn.dextea.trade.catalog.domain.exception.CatalogException;
import cn.dextea.trade.catalog.domain.model.aggregate.Customer;
import cn.dextea.trade.catalog.domain.model.aggregate.Product;
import cn.dextea.trade.catalog.domain.model.aggregate.Store;
import cn.dextea.trade.catalog.domain.model.valueobject.Customization;
import cn.dextea.trade.catalog.domain.model.valueobject.CustomizationOption;
import cn.dextea.trade.catalog.domain.model.valueobject.CustomizationOptionStoreStatus;
import cn.dextea.trade.catalog.domain.model.valueobject.ProductCover;
import cn.dextea.trade.catalog.domain.model.valueobject.ProductStoreStatus;
import cn.dextea.trade.catalog.domain.model.valueobject.UnavailableCustomization;
import cn.dextea.trade.catalog.domain.model.valueobject.UnavailableProduct;
import cn.dextea.trade.catalog.domain.repository.CatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogService {
    private final CatalogRepository catalogRepository;

    public List<Product> findProducts(List<Long> ids) {
        return catalogRepository.findProducts(ids);
    }

    public Map<Long, ProductStoreStatus> findProductStoreStatusMap(List<Long> productIds, Long storeId) {
        Map<Long, ProductStoreStatus> map = new HashMap<>();
        catalogRepository.findProductStoreStatus(productIds, storeId)
                .forEach(s -> map.put(s.getProductId(), s));
        return map;
    }

    public Map<Long, ProductCover> findProductCovers(List<Long> productIds) {
        return catalogRepository.findProductCovers(productIds);
    }

    public Map<Long, String> findCoverUrls(List<Long> coverIds) {
        return catalogRepository.findCoverUrls(coverIds);
    }

    public List<Customization> findCustomizations(List<Long> ids) {
        return catalogRepository.findCustomizations(ids);
    }

    public Map<Long, Customization> findCustomizationMap(Set<Long> ids) {
        return loadByIds(ids, catalogRepository::findCustomizations, Customization::getId,
                CatalogErrorCode.CUSTOMIZATION_ID_INVALID, "客制化项目");
    }

    public List<CustomizationOption> findOptions(List<Long> ids) {
        return catalogRepository.findOptions(ids);
    }

    public Map<Long, CustomizationOption> findOptionMap(Set<Long> ids) {
        return loadByIds(ids, catalogRepository::findOptions, CustomizationOption::getId,
                CatalogErrorCode.CUSTOMIZATION_OPTION_ID_INVALID, "客制化选项");
    }

    public Map<Long, CustomizationOptionStoreStatus> findOptionStoreStatusMap(List<Long> optionIds, Long storeId) {
        Map<Long, CustomizationOptionStoreStatus> map = new HashMap<>();
        catalogRepository.findOptionStoreStatus(optionIds, storeId)
                .forEach(s -> map.put(s.getCustomizationOptionId(), s));
        return map;
    }

    public Store findStore(Long id) {
        return catalogRepository.findStore(id);
    }

    public void assertStoreValid(Long storeId) {
        Store store = catalogRepository.findStore(storeId);
        if (store == null) {
            throw new CatalogException(CatalogErrorCode.STORE_ID_INVALID, String.valueOf(storeId));
        }
        if (!store.isOpen()) {
            throw new CatalogException(CatalogErrorCode.STORE_UNAVAILABLE, String.valueOf(storeId));
        }
    }

    public Customer findCustomer(Long id) {
        return catalogRepository.findCustomer(id);
    }

    public void assertCustomerValid(Long customerId) {
        Customer customer = catalogRepository.findCustomer(customerId);
        if (customer == null) {
            throw new CatalogException(CatalogErrorCode.CUSTOMER_ID_INVALID, String.valueOf(customerId));
        }
        if (!customer.isActive()) {
            throw new CatalogException(CatalogErrorCode.CUSTOMER_UNAVAILABLE, String.valueOf(customerId));
        }
    }

    public Map<Long, Product> findProductMap(Set<Long> productIds) {
        return loadByIds(productIds, catalogRepository::findProducts, Product::getId,
                CatalogErrorCode.PRODUCT_ID_INVALID, "商品");
    }

    public Optional<UnavailableProduct> checkProductAvailability(
            Product product, ProductStoreStatus storeStatus, Long productId) {
        if (product == null || !product.isAvailableInStore(storeStatus)) {
            return Optional.of(UnavailableProduct.builder()
                    .id(productId)
                    .name(product != null ? product.getName() : null)
                    .build());
        }
        return Optional.empty();
    }

    public List<UnavailableCustomization> checkOptionAvailability(
            List<Long> opts, List<Long> itemIdsForItem, Long productId, Product product,
            Map<Long, CustomizationOption> optionMap, Map<Long, Customization> customizationMap,
            Map<Long, CustomizationOptionStoreStatus> optionStoreStatusMap) {
        List<UnavailableCustomization> badOptions = new ArrayList<>();
        for (int j = 0; j < opts.size(); j++) {
            Long optionId = opts.get(j);
            Long itemId = itemIdsForItem.get(j);
            CustomizationOption option = optionMap.get(optionId);
            Customization customization = customizationMap.get(itemId);
            product.assertCustomizationBinding(itemId, optionId);
            boolean itemUnavailable = customization == null || !customization.isGloballyAvailable();
            boolean optionUnavailable = option == null
                    || !product.isOptionAvailableInStore(option, optionStoreStatusMap.get(optionId));
            if (itemUnavailable || optionUnavailable) {
                badOptions.add(UnavailableCustomization.builder()
                        .optionId(optionId)
                        .optionName(option != null ? option.getName() : null)
                        .productId(productId)
                        .productName(product != null ? product.getName() : null)
                        .itemId(itemId)
                        .itemName(customization != null ? customization.getName() : null)
                        .build());
            }
        }
        return badOptions;
    }

    private <T> Map<Long, T> loadByIds(
            Set<Long> ids,
            Function<List<Long>, List<T>> batchLoader,
            Function<T, Long> idExtractor,
            CatalogErrorCode errorCode,
            String entityName) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<T> entities = batchLoader.apply(new ArrayList<>(ids));
        Map<Long, T> map = entities.stream()
                .collect(Collectors.toMap(idExtractor, e -> e, (a, b) -> a));
        List<Long> notFound = ids.stream()
                .filter(id -> !map.containsKey(id))
                .toList();
        if (!notFound.isEmpty()) {
            throw new CatalogException(errorCode, entityName + "ID非法: " + notFound.stream()
                    .map(String::valueOf).collect(Collectors.joining("、")));
        }
        return map;
    }
}
