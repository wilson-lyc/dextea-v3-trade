package cn.dextea.trade.order.domain.service;

import cn.dextea.trade.catalog.domain.model.Customization;
import cn.dextea.trade.catalog.domain.model.CustomizationOption;
import cn.dextea.trade.catalog.domain.model.CustomizationOptionStoreStatus;
import cn.dextea.trade.catalog.domain.model.Customer;
import cn.dextea.trade.catalog.domain.model.Gallery;
import cn.dextea.trade.catalog.domain.model.Product;
import cn.dextea.trade.catalog.domain.model.ProductStoreStatus;
import cn.dextea.trade.catalog.domain.model.Store;
import cn.dextea.trade.common.error.BizError;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.PreBuildContext;
import cn.dextea.trade.order.domain.model.PreBuildProductInput;
import cn.dextea.trade.order.domain.model.PreBuildResult;
import cn.dextea.trade.order.domain.model.PricedOrderItem;
import cn.dextea.trade.order.domain.model.UnavailableCustomization;
import cn.dextea.trade.order.domain.model.UnavailableProduct;
import cn.dextea.trade.order.domain.port.CatalogPort;
import cn.dextea.trade.order.domain.util.SkuIdParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 订单预构建（下单前只读计价与可用性校验）领域服务。
 *
 * <p>通过商品目录防腐端口获取只读快照，完成 skuId 解析、商品/客制化可用性校验、计价与明细构建，
 * 产出 {@link PreBuildResult}。所有外部支撑数据均经端口访问，不依赖 catalog 持久化细节。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPlacementDomainService {

    private final CatalogPort catalogPort;

    public PreBuildResult preBuild(PreBuildContext ctx) {
        Long storeId = ctx.getStoreId();
        Long customerId = ctx.getCustomerId();
        List<PreBuildProductInput> items = ctx.getProducts();

        // 1. 校验门店与顾客可用性
        boolean storeAvailable = isStoreAvailable(storeId);
        boolean customerAvailable = isCustomerAvailable(customerId);
        if (!storeAvailable || !customerAvailable) {
            return PreBuildResult.builder()
                    .storeAvailable(storeAvailable)
                    .customerAvailable(customerAvailable)
                    .build();
        }

        // 2. 解析 skuId，获取商品/选项/客制化项目 ID
        SkuResolution resolution = resolveSkuIds(items);

        // 3. 批量加载所有关联实体（商品、封面、门店状态、客制化项目、选项）
        LoadedEntities entities = loadAllEntities(resolution, storeId);

        // 4. 逐项分类：商品级剔除 → 选项级剔除 → 有效商品汇总
        List<UnavailableProduct> unavailableProducts = new ArrayList<>();
        List<UnavailableCustomization> unavailableOptions = new ArrayList<>();
        Set<Long> reportedProductIds = new LinkedHashSet<>();
        Set<Long> reportedOptionIds = new LinkedHashSet<>();

        int totalQuantity = 0;
        BigDecimal totalPrice = BigDecimal.ZERO;
        List<PricedOrderItem> availableProducts = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            PreBuildProductInput item = items.get(i);
            List<Long> opts = resolution.optionIds().get(i);
            Long productId = resolution.productIds().get(i);
            Product product = entities.productMap().get(productId);

            // 商品级不可用：全局下架或门店售罄
            Optional<UnavailableProduct> productUnavailable = checkProductAvailability(
                    product, entities.productStoreStatusMap().get(productId), productId);
            if (productUnavailable.isPresent()) {
                if (reportedProductIds.add(productId)) {
                    unavailableProducts.add(productUnavailable.get());
                }
                continue;
            }

            // 选项级不可用：客制化项目非激活、选项禁用、跨绑定异常
            List<UnavailableCustomization> badOptions = checkOptionAvailability(
                    opts, resolution.itemIds().get(i), productId, product,
                    entities.optionMap(), entities.customizationMap(), entities.optionStoreStatusMap());
            if (!badOptions.isEmpty()) {
                for (UnavailableCustomization bad : badOptions) {
                    if (reportedOptionIds.add(bad.getOptionId())) {
                        unavailableOptions.add(bad);
                    }
                }
                continue;
            }

            // 有效商品：计价并构建明细
            PricedItem priced = priceItem(item, product, opts, entities.optionMap(),
                    entities.productCoverMap(), entities.productCoverUrlMap());
            totalQuantity += priced.quantity();
            totalPrice = totalPrice.add(priced.subtotal());
            availableProducts.add(priced.detail());
        }

        return PreBuildResult.builder()
                .unavailableProducts(unavailableProducts)
                .unavailableCustomizations(unavailableOptions)
                .products(availableProducts)
                .storeAvailable(true)
                .customerAvailable(true)
                .totalQuantity(totalQuantity)
                .totalPrice(totalPrice.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    private SkuResolution resolveSkuIds(List<PreBuildProductInput> items) {
        List<Long> productIds = new ArrayList<>(items.size());
        List<List<Long>> optionIds = new ArrayList<>(items.size());
        List<List<Long>> itemIds = new ArrayList<>(items.size());
        Set<Long> allProductIds = new LinkedHashSet<>();
        Set<Long> allOptionIds = new LinkedHashSet<>();
        Set<Long> allItemIds = new LinkedHashSet<>();
        for (PreBuildProductInput item : items) {
            Long productId = SkuIdParser.parseProductId(item.getSkuId());
            List<Long> opts = SkuIdParser.parseOptionIds(item.getSkuId());
            List<Long> itemsForItem = SkuIdParser.parseItemIds(item.getSkuId());
            productIds.add(productId);
            optionIds.add(opts);
            itemIds.add(itemsForItem);
            allProductIds.add(productId);
            allOptionIds.addAll(opts);
            allItemIds.addAll(itemsForItem);
        }
        return new SkuResolution(productIds, optionIds, itemIds, allProductIds, allOptionIds, allItemIds);
    }

    private LoadedEntities loadAllEntities(SkuResolution resolution, Long storeId) {
        Map<Long, Product> productMap = loadProducts(resolution.allProductIds());
        Map<Long, Long> productCoverMap = loadCoverIds(resolution.allProductIds());
        Map<Long, String> productCoverUrlMap = loadCoverUrls(productCoverMap);
        Map<Long, ProductStoreStatus> productStoreStatusMap = loadProductStoreStatus(resolution.allProductIds(), storeId);
        Map<Long, Customization> customizationMap = loadCustomizations(resolution.allItemIds());
        Map<Long, CustomizationOption> optionMap = loadOptions(resolution.allOptionIds());
        Map<Long, CustomizationOptionStoreStatus> optionStoreStatusMap = loadOptionStoreStatus(resolution.allOptionIds(), storeId);
        return new LoadedEntities(productMap, productCoverMap, productCoverUrlMap,
                productStoreStatusMap, customizationMap, optionMap, optionStoreStatusMap);
    }

    private Optional<UnavailableProduct> checkProductAvailability(
            Product product, ProductStoreStatus storeStatus, Long productId) {
        if (product == null || !product.isAvailableInStore(storeStatus)) {
            return Optional.of(UnavailableProduct.builder()
                    .id(productId)
                    .name(product != null ? product.getName() : null)
                    .build());
        }
        return Optional.empty();
    }

    private List<UnavailableCustomization> checkOptionAvailability(
            List<Long> opts, List<Long> itemIdsForItem, Long productId, Product product,
            Map<Long, CustomizationOption> optionMap, Map<Long, Customization> customizationMap,
            Map<Long, CustomizationOptionStoreStatus> optionStoreStatusMap) {
        List<UnavailableCustomization> badOptions = new ArrayList<>();
        for (int j = 0; j < opts.size(); j++) {
            Long optionId = opts.get(j);
            Long itemId = itemIdsForItem.get(j);
            CustomizationOption option = optionMap.get(optionId);
            Customization customization = customizationMap.get(itemId);

            // 跨绑定校验：客制化项目必须属于当前商品，客制化选项必须属于当前客制化项目
            boolean itemNotBelongToProduct = customization != null && customization.getProductId() != null
                    && !customization.getProductId().equals(productId);
            boolean optionNotBelongToItem = option != null && option.getCustomizationId() != null
                    && !option.getCustomizationId().equals(itemId);

            boolean itemUnavailable = customization == null || !customization.isGloballyAvailable();
            boolean optionUnavailable = option == null || !option.isAvailableInStore(optionStoreStatusMap.get(optionId));
            if (itemUnavailable || optionUnavailable || itemNotBelongToProduct || optionNotBelongToItem) {
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

    private PricedItem priceItem(PreBuildProductInput item, Product product, List<Long> opts,
            Map<Long, CustomizationOption> optionMap, Map<Long, Long> productCoverMap,
            Map<Long, String> productCoverUrlMap) {
        int quantity = item.getQuantity();
        BigDecimal unitPrice = nullToZero(product.getPrice());
        for (Long optionId : opts) {
            unitPrice = unitPrice.add(nullToZero(optionMap.get(optionId).getPrice()));
        }
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        String customizationText = opts.stream()
                .map(optionMap::get)
                .filter(Objects::nonNull)
                .map(CustomizationOption::getName)
                .filter(name -> name != null && !name.isBlank())
                .collect(Collectors.joining(" / "));
        PricedOrderItem detail = PricedOrderItem.builder()
                .skuId(item.getSkuId())
                .quantity(quantity)
                .productId(product.getId())
                .productName(product.getName())
                .coverId(productCoverMap.get(product.getId()))
                .coverUrl(productCoverUrlMap.get(product.getId()))
                .customizationText(customizationText.isEmpty() ? null : customizationText)
                .unitPrice(unitPrice.setScale(2, RoundingMode.HALF_UP))
                .subtotal(subtotal.setScale(2, RoundingMode.HALF_UP))
                .build();
        return new PricedItem(detail, quantity, subtotal);
    }

    private boolean isStoreAvailable(Long storeId) {
        Store store = catalogPort.findStore(storeId);
        return store != null && store.isOpen();
    }

    private boolean isCustomerAvailable(Long customerId) {
        Customer customer = catalogPort.findCustomer(customerId);
        return customer != null && customer.isActive();
    }

    private <T> Map<Long, T> loadByIds(
            Set<Long> ids,
            Function<List<Long>, List<T>> batchLoader,
            Function<T, Long> idExtractor,
            OrderErrorCode errorCode,
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
            throw new BizError(errorCode, entityName + "ID错误: " + join(notFound));
        }
        return map;
    }

    private Map<Long, Product> loadProducts(Set<Long> productIds) {
        return loadByIds(productIds, catalogPort::findProducts, Product::getId,
                OrderErrorCode.PRODUCT_NOT_FOUND, "商品");
    }

    private Map<Long, Long> loadCoverIds(Set<Long> productIds) {
        Map<Long, Long> map = new HashMap<>();
        if (productIds.isEmpty()) {
            return map;
        }
        catalogPort.findCoverImages(new ArrayList<>(productIds))
                .forEach(pi -> map.putIfAbsent(pi.getProductId(), pi.getImageId()));
        return map;
    }

    private Map<Long, String> loadCoverUrls(Map<Long, Long> productCoverMap) {
        Map<Long, String> map = new HashMap<>();
        if (productCoverMap == null || productCoverMap.isEmpty()) {
            return map;
        }
        Set<Long> imageIds = new LinkedHashSet<>(productCoverMap.values());
        if (imageIds.isEmpty()) {
            return map;
        }
        Map<Long, String> urlMap = catalogPort.findGalleries(new ArrayList<>(imageIds)).stream()
                .collect(Collectors.toMap(Gallery::getId, Gallery::getUrl, (a, b) -> a));
        productCoverMap.forEach((productId, imageId) -> {
            String url = urlMap.get(imageId);
            if (url != null) {
                map.put(productId, url);
            }
        });
        return map;
    }

    private Map<Long, ProductStoreStatus> loadProductStoreStatus(Set<Long> productIds, Long storeId) {
        Map<Long, ProductStoreStatus> map = new HashMap<>();
        if (productIds.isEmpty()) {
            return map;
        }
        catalogPort.findProductStoreStatus(new ArrayList<>(productIds), storeId)
                .forEach(s -> map.put(s.getProductId(), s));
        return map;
    }

    private Map<Long, Customization> loadCustomizations(Set<Long> itemIds) {
        return loadByIds(itemIds, catalogPort::findCustomizations, Customization::getId,
                OrderErrorCode.CUSTOMIZATION_NOT_FOUND, "客制化项目");
    }

    private Map<Long, CustomizationOption> loadOptions(Set<Long> optionIds) {
        return loadByIds(optionIds, catalogPort::findOptions, CustomizationOption::getId,
                OrderErrorCode.CUSTOMIZATION_OPTION_NOT_FOUND, "客制化选项");
    }

    private Map<Long, CustomizationOptionStoreStatus> loadOptionStoreStatus(Set<Long> optionIds, Long storeId) {
        Map<Long, CustomizationOptionStoreStatus> map = new HashMap<>();
        if (optionIds.isEmpty()) {
            return map;
        }
        catalogPort.findOptionStoreStatus(new ArrayList<>(optionIds), storeId)
                .forEach(s -> map.put(s.getCustomizationOptionId(), s));
        return map;
    }

    private static BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String join(List<Long> ids) {
        return ids.stream().map(String::valueOf).collect(Collectors.joining("、"));
    }

    private record SkuResolution(
            List<Long> productIds,
            List<List<Long>> optionIds,
            List<List<Long>> itemIds,
            Set<Long> allProductIds,
            Set<Long> allOptionIds,
            Set<Long> allItemIds) {}

    private record LoadedEntities(
            Map<Long, Product> productMap,
            Map<Long, Long> productCoverMap,
            Map<Long, String> productCoverUrlMap,
            Map<Long, ProductStoreStatus> productStoreStatusMap,
            Map<Long, Customization> customizationMap,
            Map<Long, CustomizationOption> optionMap,
            Map<Long, CustomizationOptionStoreStatus> optionStoreStatusMap) {}

    private record PricedItem(PricedOrderItem detail, int quantity, BigDecimal subtotal) {}
}
