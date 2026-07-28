package cn.dextea.trade.order.domain.service;

import cn.dextea.trade.common.error.BizError;
import cn.dextea.trade.order.domain.enums.DiningMethodEnum;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.gateway.CustomerGateway;
import cn.dextea.trade.order.domain.gateway.CustomizationGateway;
import cn.dextea.trade.order.domain.gateway.PaymentClientGateway;
import cn.dextea.trade.order.domain.gateway.ProductCover;
import cn.dextea.trade.order.domain.gateway.ProductGateway;
import cn.dextea.trade.order.domain.gateway.StoreGateway;
import cn.dextea.trade.order.domain.model.aggregate.Order;
import cn.dextea.trade.order.domain.model.valueobject.PreBuildContext;
import cn.dextea.trade.order.domain.model.valueobject.PreBuildProductInput;
import cn.dextea.trade.order.domain.model.valueobject.PreBuildResult;
import cn.dextea.trade.order.domain.model.valueobject.PricedOrderItem;
import cn.dextea.trade.order.domain.model.valueobject.UnavailableCustomization;
import cn.dextea.trade.order.domain.model.valueobject.UnavailableProduct;
import cn.dextea.trade.order.domain.model.valueobject.Customer;
import cn.dextea.trade.order.domain.model.valueobject.Customization;
import cn.dextea.trade.order.domain.model.valueobject.CustomizationOption;
import cn.dextea.trade.order.domain.model.valueobject.CustomizationOptionStoreStatus;
import cn.dextea.trade.order.domain.model.valueobject.Product;
import cn.dextea.trade.order.domain.model.valueobject.ProductStoreStatus;
import cn.dextea.trade.order.domain.model.valueobject.Store;
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

/**
 * 订单预构建（下单前只读计价与可用性校验）领域服务。
 *
 * <p>通过商品/客制化/门店/顾客四个领域网关获取只读快照，完成 skuId 解析、
 * 商品/客制化可用性校验、计价与明细构建，产出 {@link PreBuildResult}。
 * 所有外部支撑数据均经网关接口访问，不依赖外部域持久化细节
 * （如图库表结构由基础设施层清洗为 productId → 封面 的映射后提供）。</p>
 */
@Slf4j
@RequiredArgsConstructor
public class OrderPlacementDomainService {

    private final ProductGateway productGateway;
    private final CustomizationGateway customizationGateway;
    private final StoreGateway storeGateway;
    private final CustomerGateway customerGateway;
    private final PaymentClientGateway paymentClientGateway;

    /**
     * 支付平台编码（整型跨域契约，与 pay 域 {@code PlatformEnum} 的 code 约定保持一致）。
     * 订单域仅以整型视图消费该契约，不依赖 pay 域类型，从而保持限界上下文自封闭。
     * 当前仅开放支付宝（2），微信（1）暂未实现。
     */
    private static final int ALIPAY_PLATFORM_CODE = 2;

    public PreBuildResult preBuild(PreBuildContext ctx) {
        Long storeId = ctx.getStoreId();
        Long customerId = ctx.getCustomerId();
        List<PreBuildProductInput> items = ctx.getProducts();

        // 1. 校验门店与顾客：ID 非法或不可用直接抛业务异常（与下单逻辑统一）
        validateStore(storeId);
        validateCustomer(customerId);

        // 2. 解析 skuId，获取商品/选项/客制化项目 ID
        SkuResolution resolution = resolveSkuIds(items);

        // 3. 批量加载所有关联快照（商品、封面、门店状态、客制化项目、选项）
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

    /**
     * 下单前领域规则校验：支付平台可用性与用餐方式合法性。
     *
     * <p>这两项均属订单上下文的不变式：当前仅开放支付宝、暂不支持微信；用餐方式必须命中
     * 领域枚举。原本散落在应用层的平台拦截与用餐方式校验收归此处，使业务规则沉淀在领域层，
     * 应用层无需关心具体判据。校验失败抛出对应业务异常交由上层转换为错误响应。</p>
     *
     * @param platformCode 支付平台编码（整型契约，由应用层从 {@code PlatformEnum} 翻译而来）
     */
    public void validatePlacement(int platformCode, Integer diningMethodCode) {
        if (!isPlatformSupported(platformCode)) {
            throw new BizError(OrderErrorCode.PAY_PLATFORM_NOT_SUPPORTED, "暂不支持的支付方式: " + platformCode);
        }
        DiningMethodEnum diningMethod = DiningMethodEnum.of(diningMethodCode);
        if (diningMethod == null) {
            throw new BizError(OrderErrorCode.DINING_METHOD_INVALID, "用餐方式错误: " + diningMethodCode);
        }
    }

    /**
     * 发起支付：封装「是否需要立即创建交易」的业务决策与「顾客须已绑定支付宝」的不变式。
     *
     * <p>当前仅支付宝需要在下单时同步创建交易并回填 {@code trade_no}；微信暂未开放（已于
     * {@link #validatePlacement} 拦截）。方法通过领域网关获取顾客快照、经支付网关创建交易，
     * 并以聚合行为方法 {@link Order#markTradeNo} 写入交易号，调用方（应用层）仅负责持久化回填。</p>
     */
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
                order.getTotalQuantity(), order.getPayMethod(), order.getPayExpireAt());
        order.markTradeNo(tradeNo);
    }

    /**
     * 当前支付方式是否需要在下单时立即创建支付交易。
     */
    private boolean needsImmediatePayment(Integer payMethod) {
        return ALIPAY_PLATFORM_CODE == payMethod;
    }

    /**
     * 支付平台可用性策略：当前仅开放支付宝。
     */
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

            // 跨绑定校验：客制化项目必须属于当前商品，客制化选项必须属于当前客制化项目
            boolean itemNotBelongToProduct = customization != null && customization.getProductId() != null
                    && !customization.getProductId().equals(productId);
            boolean optionNotBelongToItem = option != null && option.getCustomizationId() != null
                    && !option.getCustomizationId().equals(itemId);

            // 绑定关系非法：直接抛异常，交由上层转换为业务错误（不入不可用项）
            if (itemNotBelongToProduct) {
                throw new BizError(OrderErrorCode.CUSTOMIZATION_BINDING_INVALID,
                        "客制化项目" + itemId + "不属于商品" + productId);
            }
            if (optionNotBelongToItem) {
                throw new BizError(OrderErrorCode.CUSTOMIZATION_BINDING_INVALID,
                        "客制化选项" + optionId + "不属于客制化项目" + itemId);
            }

            // 合法但下架/禁用：进入不可用项，供前端提示用户删除
            boolean itemUnavailable = customization == null || !customization.isGloballyAvailable();
            boolean optionUnavailable = option == null || !option.isAvailableInStore(optionStoreStatusMap.get(optionId));
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
            throw new BizError(errorCode, entityName + "ID错误: " + join(notFound));
        }
        return map;
    }

    private Map<Long, Product> loadProducts(Set<Long> productIds) {
        return loadByIds(productIds, productGateway::findProducts, Product::getId,
                OrderErrorCode.PRODUCT_NOT_FOUND, "商品");
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
                OrderErrorCode.CUSTOMIZATION_NOT_FOUND, "客制化项目");
    }

    private Map<Long, CustomizationOption> loadOptions(Set<Long> optionIds) {
        return loadByIds(optionIds, customizationGateway::findOptions, CustomizationOption::getId,
                OrderErrorCode.CUSTOMIZATION_OPTION_NOT_FOUND, "客制化选项");
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
