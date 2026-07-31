package cn.dextea.trade.order.domain.service;
import cn.dextea.trade.shared.domain.error.BizError;
import cn.dextea.trade.order.domain.enums.DiningMethodEnum;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.gateway.CustomerGateway;
import cn.dextea.trade.order.domain.gateway.CustomizationGateway;
import cn.dextea.trade.order.domain.gateway.PaymentClientGateway;
import cn.dextea.trade.catalog.domain.model.valueobject.ProductCover;
import cn.dextea.trade.order.domain.gateway.ProductGateway;
import cn.dextea.trade.order.domain.gateway.StoreGateway;
import cn.dextea.trade.order.domain.model.aggregate.Order;
import cn.dextea.trade.catalog.domain.model.aggregate.Customer;
import cn.dextea.trade.order.domain.model.valueobject.PaymentMethod;
import cn.dextea.trade.catalog.domain.model.aggregate.Product;
import cn.dextea.trade.catalog.domain.model.aggregate.Store;
import cn.dextea.trade.catalog.domain.model.valueobject.Customization;
import cn.dextea.trade.catalog.domain.model.valueobject.CustomizationOption;
import cn.dextea.trade.catalog.domain.model.valueobject.CustomizationOptionStoreStatus;
import cn.dextea.trade.order.domain.model.valueobject.PreBuildContext;
import cn.dextea.trade.order.domain.model.valueobject.PreBuildProductInput;
import cn.dextea.trade.order.domain.model.valueobject.PreBuildResult;
import cn.dextea.trade.order.domain.model.valueobject.PricedOrderItem;
import cn.dextea.trade.catalog.domain.model.valueobject.ProductStoreStatus;
import cn.dextea.trade.catalog.domain.model.valueobject.UnavailableCustomization;
import cn.dextea.trade.catalog.domain.model.valueobject.UnavailableProduct;
import cn.dextea.trade.order.domain.util.SkuIdParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RequiredArgsConstructor
public class OrderPlacementDomainService {
    private final ProductGateway productGateway;
    private final CustomizationGateway customizationGateway;
    private final StoreGateway storeGateway;
    private final CustomerGateway customerGateway;
    private final PaymentClientGateway paymentClientGateway;
    private static final int ALIPAY_PLATFORM_CODE = 2;
    public PreBuildResult preBuild(PreBuildContext ctx) {
        Long storeId = ctx.getStoreId();
        Long customerId = ctx.getCustomerId();
        List<PreBuildProductInput> items = ctx.getProducts();
        validateStore(storeId);
        validateCustomer(customerId);
        SkuResolution resolution = resolveSkuIds(items);
        LoadedEntities entities = loadAllEntities(resolution, storeId);
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
            Optional<UnavailableProduct> productUnavailable = checkProductAvailability(
                    product, entities.productStoreStatusMap().get(productId), productId);
            if (productUnavailable.isPresent()) {
                if (reportedProductIds.add(productId)) {
                    unavailableProducts.add(productUnavailable.get());
                }
                continue;
            }
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
            PricedItem priced = priceItem(item, product, opts, entities.optionMap(),
                    entities.productCoverMap());
            totalQuantity += priced.quantity();
            totalPrice = totalPrice.add(priced.subtotal());
            availableProducts.add(priced.detail());
        }
        return PreBuildResult.builder()
                .unavailableProducts(unavailableProducts)
                .unavailableCustomizations(unavailableOptions)
                .products(availableProducts)
                .totalQuantity(totalQuantity)
                .totalPrice(totalPrice.setScale(2, RoundingMode.HALF_UP))
                .build();
    }
    public void validatePlacement(int platformCode, Integer diningMethodCode) {
        if (!isPlatformSupported(platformCode)) {
            throw new BizError(OrderErrorCode.PAY_PLATFORM_NOT_SUPPORTED, "暂不支持的支付方式: " + platformCode);
        }
        DiningMethodEnum diningMethod = DiningMethodEnum.of(diningMethodCode);
        if (diningMethod == null) {
            throw new BizError(OrderErrorCode.DINING_METHOD_INVALID, "用餐方式错误: " + diningMethodCode);
        }
    }
    public void initiatePayment(Order order) {
        if (!needsImmediatePayment(order.getPayMethod())) {
            return;
        }
        Customer customer = customerGateway.findCustomer(order.getCustomerId());
        if (customer == null || customer.getAlipayOpenId() == null) {
            throw new BizError(OrderErrorCode.ALIPAY_BUYER_NOT_BOUND, "顾客未绑定支付宝，无法创建支付");
        }
        String tradeNo = paymentClientGateway.createPayment(
                order.getOrderNo(), order.getTotalPrice(), customer.getAlipayOpenId(),
                order.getTotalQuantity(), order.getPayMethod().getCode(), order.getPayExpireAt());
        order.markTradeNo(tradeNo);
    }
    private boolean needsImmediatePayment(PaymentMethod payMethod) {
        return ALIPAY_PLATFORM_CODE == payMethod.getCode();
    }
    private boolean isPlatformSupported(int platformCode) {
        return ALIPAY_PLATFORM_CODE == platformCode;
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
        Map<Long, ProductCover> productCoverMap = loadProductCovers(resolution.allProductIds());
        Map<Long, ProductStoreStatus> productStoreStatusMap = loadProductStoreStatus(resolution.allProductIds(), storeId);
        Map<Long, Customization> customizationMap = loadCustomizations(resolution.allItemIds());
        Map<Long, CustomizationOption> optionMap = loadOptions(resolution.allOptionIds());
        Map<Long, CustomizationOptionStoreStatus> optionStoreStatusMap = loadOptionStoreStatus(resolution.allOptionIds(), storeId);
        return new LoadedEntities(productMap, productCoverMap,
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
            if (!product.isCustomizationBelongToProduct(itemId)) {
                throw new BizError(OrderErrorCode.CUSTOMIZATION_BINDING_INVALID);
            }
            if (!product.isOptionBelongToCustomization(itemId, optionId)) {
                throw new BizError(OrderErrorCode.CUSTOMIZATION_BINDING_INVALID);
            }
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
    private PricedItem priceItem(PreBuildProductInput item, Product product, List<Long> opts,
            Map<Long, CustomizationOption> optionMap, Map<Long, ProductCover> productCoverMap) {
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
        ProductCover cover = productCoverMap.get(product.getId());
        PricedOrderItem detail = PricedOrderItem.builder()
                .skuId(item.getSkuId())
                .quantity(quantity)
                .productId(product.getId())
                .productName(product.getName())
                .coverId(cover != null ? cover.coverId() : null)
                .coverUrl(cover != null ? cover.coverUrl() : null)
                .customizationText(customizationText.isEmpty() ? null : customizationText)
                .unitPrice(unitPrice.setScale(2, RoundingMode.HALF_UP))
                .subtotal(subtotal.setScale(2, RoundingMode.HALF_UP))
                .build();
        return new PricedItem(detail, quantity, subtotal);
    }
    private void validateStore(Long storeId) {
        Store store = storeGateway.findStore(storeId);
        if (store == null) {
            throw new BizError(OrderErrorCode.STORE_ID_INVALID, "门店ID非法: " + storeId);
        }
        if (!store.isOpen()) {
            throw new BizError(OrderErrorCode.STORE_UNAVAILABLE, "门店不可用，无法下单: " + storeId);
        }
    }
    private void validateCustomer(Long customerId) {
        Customer customer = customerGateway.findCustomer(customerId);
        if (customer == null) {
            throw new BizError(OrderErrorCode.CUSTOMER_ID_INVALID, "顾客ID非法: " + customerId);
        }
        if (!customer.isActive()) {
            throw new BizError(OrderErrorCode.CUSTOMER_UNAVAILABLE, "顾客不可用，无法下单: " + customerId);
        }
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
            throw new BizError(errorCode, entityName + "ID非法: " + join(notFound));
        }
        return map;
    }
    private Map<Long, Product> loadProducts(Set<Long> productIds) {
        return loadByIds(productIds, productGateway::findProducts, Product::getId,
                OrderErrorCode.PRODUCT_ID_INVALID, "商品");
    }
    private Map<Long, ProductCover> loadProductCovers(Set<Long> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }
        return productGateway.findProductCovers(new ArrayList<>(productIds));
    }
    private Map<Long, ProductStoreStatus> loadProductStoreStatus(Set<Long> productIds, Long storeId) {
        Map<Long, ProductStoreStatus> map = new HashMap<>();
        if (productIds.isEmpty()) {
            return map;
        }
        productGateway.findProductStoreStatus(new ArrayList<>(productIds), storeId)
                .forEach(s -> map.put(s.getProductId(), s));
        return map;
    }
    private Map<Long, Customization> loadCustomizations(Set<Long> itemIds) {
        return loadByIds(itemIds, customizationGateway::findCustomizations, Customization::getId,
                OrderErrorCode.CUSTOMIZATION_ID_INVALID, "客制化项目");
    }
    private Map<Long, CustomizationOption> loadOptions(Set<Long> optionIds) {
        return loadByIds(optionIds, customizationGateway::findOptions, CustomizationOption::getId,
                OrderErrorCode.CUSTOMIZATION_OPTION_ID_INVALID, "客制化选项");
    }
    private Map<Long, CustomizationOptionStoreStatus> loadOptionStoreStatus(Set<Long> optionIds, Long storeId) {
        Map<Long, CustomizationOptionStoreStatus> map = new HashMap<>();
        if (optionIds.isEmpty()) {
            return map;
        }
        customizationGateway.findOptionStoreStatus(new ArrayList<>(optionIds), storeId)
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
            Map<Long, ProductCover> productCoverMap,
            Map<Long, ProductStoreStatus> productStoreStatusMap,
            Map<Long, Customization> customizationMap,
            Map<Long, CustomizationOption> optionMap,
            Map<Long, CustomizationOptionStoreStatus> optionStoreStatusMap) {}
    private record PricedItem(PricedOrderItem detail, int quantity, BigDecimal subtotal) {}
}
