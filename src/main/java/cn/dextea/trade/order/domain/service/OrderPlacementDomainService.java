package cn.dextea.trade.order.domain.service;

import cn.dextea.trade.catalog.domain.enums.CustomizationOptionGlobalStatusEnum;
import cn.dextea.trade.catalog.domain.enums.CustomizationStatusEnum;
import cn.dextea.trade.catalog.domain.enums.ProductGlobalStatusEnum;
import cn.dextea.trade.catalog.domain.enums.ProductStoreStatusEnum;
import cn.dextea.trade.catalog.domain.enums.StoreStatusEnum;
import cn.dextea.trade.catalog.domain.enums.CustomerStatusEnum;
import cn.dextea.trade.catalog.domain.model.Customization;
import cn.dextea.trade.catalog.domain.model.CustomizationOption;
import cn.dextea.trade.catalog.domain.model.Gallery;
import cn.dextea.trade.catalog.domain.model.Product;
import cn.dextea.trade.catalog.domain.model.Store;
import cn.dextea.trade.catalog.domain.model.Customer;
import cn.dextea.trade.exception.BizError;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.PreBuildContext;
import cn.dextea.trade.order.domain.model.PreBuildProductInput;
import cn.dextea.trade.order.domain.model.PreBuildResult;
import cn.dextea.trade.order.domain.model.PricedOrderItem;
import cn.dextea.trade.order.domain.model.UnavailableCustomization;
import cn.dextea.trade.order.domain.model.UnavailableProduct;
import cn.dextea.trade.order.domain.port.CustomerPort;
import cn.dextea.trade.order.domain.port.ProductCatalogPort;
import cn.dextea.trade.order.domain.port.StorePort;
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

    private final ProductCatalogPort productCatalogPort;
    private final StorePort storePort;
    private final CustomerPort customerPort;

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
        Map<Long, Integer> productStoreStatusMap = loadProductStoreStatus(resolution.allProductIds(), storeId);
        Map<Long, Customization> customizationMap = loadCustomizations(resolution.allItemIds());
        Map<Long, CustomizationOption> optionMap = loadOptions(resolution.allOptionIds());
        Map<Long, Integer> optionStoreStatusMap = loadOptionStoreStatus(resolution.allOptionIds(), storeId);
        return new LoadedEntities(productMap, productCoverMap, productCoverUrlMap,
                productStoreStatusMap, customizationMap, optionMap, optionStoreStatusMap);
    }

    private Optional<UnavailableProduct> checkProductAvailability(
            Product product, Integer storeStatus, Long productId) {
        if (isProductUnavailable(product, storeStatus)) {
            return Optional.of(UnavailableProduct.builder()
                    .id(productId)
                    .name(product.getName())
                    .build());
        }
        return Optional.empty();
    }

    private List<UnavailableCustomization> checkOptionAvailability(
            List<Long> opts, List<Long> itemIdsForItem, Long productId, Product product,
            Map<Long, CustomizationOption> optionMap, Map<Long, Customization> customizationMap,
            Map<Long, Integer> optionStoreStatusMap) {
        List<UnavailableCustomization> badOptions = new ArrayList<>();
        for (int j = 0; j < opts.size(); j++) {
            Long optionId = opts.get(j);
            Long itemId = itemIdsForItem.get(j);
            CustomizationOption option = optionMap.get(optionId);
            Customization customization = customizationMap.get(itemId);

            // 跨绑定校验：客制化项目必须属于当前商品，客制化选项必须属于当前客制化项目
            boolean itemNotBelongToProduct = customization.getProductId() != null
                    && !customization.getProductId().equals(productId);
            boolean optionNotBelongToItem = option.getCustomizationId() != null
                    && !option.getCustomizationId().equals(itemId);

            boolean itemUnavailable = isCustomizationUnavailable(customization);
            boolean optionUnavailable = isOptionUnavailable(option, optionStoreStatusMap.get(optionId));
            if (itemUnavailable || optionUnavailable || itemNotBelongToProduct || optionNotBelongToItem) {
                badOptions.add(UnavailableCustomization.builder()
                        .optionId(optionId)
                        .optionName(option.getName())
                        .productId(productId)
                        .productName(product.getName())
                        .itemId(itemId)
                        .itemName(customization.getName())
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
        Store store = storePort.findById(storeId);
        if (store == null) {
            return false;
        }
        Integer status = store.getStatus();
        return status != null && status == StoreStatusEnum.OPEN.getCode();
    }

    private boolean isCustomerAvailable(Long customerId) {
        Customer customer = customerPort.findById(customerId);
        if (customer == null) {
            return false;
        }
        Integer status = customer.getStatus();
        return status != null && status == CustomerStatusEnum.ACTIVE.getCode();
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
        return loadByIds(productIds, productCatalogPort::findProducts, Product::getId,
                OrderErrorCode.PRODUCT_NOT_FOUND, "商品");
    }

    private Map<Long, Long> loadCoverIds(Set<Long> productIds) {
        Map<Long, Long> map = new HashMap<>();
        if (productIds.isEmpty()) {
            return map;
        }
        productCatalogPort.findCoverImages(new ArrayList<>(productIds))
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
        Map<Long, String> urlMap = productCatalogPort.findGalleries(new ArrayList<>(imageIds)).stream()
                .collect(Collectors.toMap(Gallery::getId, Gallery::getUrl, (a, b) -> a));
        productCoverMap.forEach((productId, imageId) -> {
            String url = urlMap.get(imageId);
            if (url != null) {
                map.put(productId, url);
            }
        });
        return map;
    }

    private Map<Long, Integer> loadProductStoreStatus(Set<Long> productIds, Long storeId) {
        Map<Long, Integer> map = new HashMap<>();
        if (productIds.isEmpty()) {
            return map;
        }
        productCatalogPort.findProductStoreStatus(new ArrayList<>(productIds), storeId)
                .forEach(s -> map.put(s.getProductId(), s.getStatus()));
        return map;
    }

    private Map<Long, Customization> loadCustomizations(Set<Long> itemIds) {
        return loadByIds(itemIds, productCatalogPort::findCustomizations, Customization::getId,
                OrderErrorCode.CUSTOMIZATION_NOT_FOUND, "客制化项目");
    }

    private Map<Long, CustomizationOption> loadOptions(Set<Long> optionIds) {
        return loadByIds(optionIds, productCatalogPort::findOptions, CustomizationOption::getId,
                OrderErrorCode.CUSTOMIZATION_OPTION_NOT_FOUND, "客制化选项");
    }

    private Map<Long, Integer> loadOptionStoreStatus(Set<Long> optionIds, Long storeId) {
        Map<Long, Integer> map = new HashMap<>();
        if (optionIds.isEmpty()) {
            return map;
        }
        productCatalogPort.findOptionStoreStatus(new ArrayList<>(optionIds), storeId)
                .forEach(s -> map.put(s.getCustomizationOptionId(), s.getStatus()));
        return map;
    }

    private boolean isProductUnavailable(Product product, Integer storeStatus) {
        boolean globalOffShelf = product.getStatus() == null
                || product.getStatus() != ProductGlobalStatusEnum.ON_SHELF.getCode();
        boolean storeSoldOut = storeStatus == null
                || storeStatus != ProductStoreStatusEnum.AVAILABLE.getCode();
        return globalOffShelf || storeSoldOut;
    }

    private boolean isOptionUnavailable(CustomizationOption option, Integer storeStatus) {
        boolean globalDisabled = option.getStatus() == null
                || option.getStatus() != CustomizationOptionGlobalStatusEnum.ACTIVE.getCode();
        boolean storeDisabled = storeStatus == null
                || storeStatus != CustomizationOptionGlobalStatusEnum.ACTIVE.getCode();
        return globalDisabled || storeDisabled;
    }

    private boolean isCustomizationUnavailable(Customization customization) {
        return customization == null
                || customization.getStatus() == null
                || customization.getStatus() != CustomizationStatusEnum.ACTIVE.getCode();
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
            Map<Long, Integer> productStoreStatusMap,
            Map<Long, Customization> customizationMap,
            Map<Long, CustomizationOption> optionMap,
            Map<Long, Integer> optionStoreStatusMap) {}

    private record PricedItem(PricedOrderItem detail, int quantity, BigDecimal subtotal) {}
}
