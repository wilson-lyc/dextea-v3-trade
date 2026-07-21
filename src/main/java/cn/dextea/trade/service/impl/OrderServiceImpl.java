package cn.dextea.trade.service.impl;

import cn.dextea.trade.common.BizError;
import cn.dextea.trade.dto.CreateOrderRequest;
import cn.dextea.trade.dto.CalculateOrderResponse;
import cn.dextea.trade.dto.CreateOrderResponse;
import cn.dextea.trade.dto.CreateOrderUnavailable;
import cn.dextea.trade.dto.CreateOrderProductItem;
import cn.dextea.trade.dto.CreateOrderUnavailableCustomization;
import cn.dextea.trade.dto.CreateOrderUnavailableProduct;
import cn.dextea.trade.entity.Customization;
import cn.dextea.trade.entity.CustomizationOption;
import cn.dextea.trade.entity.Order;
import cn.dextea.trade.entity.Product;
import cn.dextea.trade.entity.enums.CustomizationOptionGlobalStatus;
import cn.dextea.trade.entity.enums.CustomizationStatus;
import cn.dextea.trade.entity.enums.OrderStatus;
import cn.dextea.trade.entity.enums.ProductGlobalStatus;
import cn.dextea.trade.entity.enums.ProductStoreStatusEnum;
import cn.dextea.trade.error.OrderErrorCode;
import cn.dextea.trade.mapper.CustomizationMapper;
import cn.dextea.trade.mapper.CustomizationOptionMapper;
import cn.dextea.trade.mapper.CustomizationOptionStoreStatusMapper;
import cn.dextea.trade.mapper.OrderMapper;
import cn.dextea.trade.mapper.ProductMapper;
import cn.dextea.trade.mapper.ProductStoreStatusMapper;
import cn.dextea.trade.service.OrderService;
import cn.dextea.trade.util.SkuIdParser;
import lombok.RequiredArgsConstructor;
import me.ahoo.cosid.provider.IdGeneratorProvider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final ProductMapper productMapper;
    private final ProductStoreStatusMapper productStoreStatusMapper;
    private final CustomizationMapper customizationMapper;
    private final CustomizationOptionMapper customizationOptionMapper;
    private final CustomizationOptionStoreStatusMapper customizationOptionStoreStatusMapper;
    private final OrderMapper orderMapper;
    private final IdGeneratorProvider idGeneratorProvider;

    /**
     * 创建订单：复用计价逻辑，存在不可用项时不落库并返回空单号；否则生成订单号并落库。
     */
    @Override
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        // 复用订单计价逻辑，得到剔除不可用项后的总数量/总金额/不可用清单
        CalculateOrderResponse summary = computeOrder(request);

        // 存在不可用项时不创建订单记录，id 与 tradeNo 置空返回
        if (hasUnavailable(summary.getUnavailable())) {
            return CreateOrderResponse.builder()
                    .id(null)
                    .tradeNo(null)
                    .totalQuantity(summary.getTotalQuantity())
                    .totalPrice(summary.getTotalPrice())
                    .unavailable(summary.getUnavailable())
                    .build();
        }

        // 用 CosID 雪花算法（Redis 分配机器号）生成订单号
        String orderNo = idGeneratorProvider.getShare().generateAsString();

        // tradeNo 暂用 orderNo 代替，待接入微信/支付宝支付渠道后替换为渠道返回的交易号
        LocalDateTime now = LocalDateTime.now();
        Order order = Order.builder()
                .orderNo(orderNo)
                .tradeNo(orderNo)
                .customerId(request.getCustomerId())
                .storeId(request.getStoreId())
                .status(OrderStatus.PENDING.getCode())
                .payMethod(request.getPlatform().getPayMethod().getCode())
                .price(summary.getTotalPrice())
                .createdAt(now)
                .updatedAt(now)
                .build();
        orderMapper.insert(order);

        return CreateOrderResponse.builder()
                .id(order.getId())
                .tradeNo(order.getTradeNo())
                .totalQuantity(summary.getTotalQuantity())
                .totalPrice(summary.getTotalPrice())
                .unavailable(summary.getUnavailable())
                .build();
    }

    /**
     * 判断是否存在不可用项（下架/缺货商品或禁用客制化选项）。
     */
    private static boolean hasUnavailable(CreateOrderUnavailable unavailable) {
        if (unavailable == null) {
            return false;
        }
        boolean products = unavailable.getProducts() != null && !unavailable.getProducts().isEmpty();
        boolean customization = unavailable.getCustomization() != null && !unavailable.getCustomization().isEmpty();
        return products || customization;
    }

    /**
     * 计算订单价格：直接复用内部计价逻辑，返回可用项汇总与不可用清单。
     */
    @Override
    public CalculateOrderResponse calculate(CreateOrderRequest request) {
        return computeOrder(request);
    }

    /**
     * 订单计价核心逻辑：解析 skuId、校验商品与客制化选项的存在性与门店状态，
     * 剔除不可用项并汇总有效商品的总数量与总金额。
     */
    private CalculateOrderResponse computeOrder(CreateOrderRequest request) {
        List<CreateOrderProductItem> items = request.getProducts();
        Long storeId = request.getStoreId();

        // 解析 skuId，收集商品ID、客制化项目ID与客制化选项ID
        List<List<Long>> parsedOptionIds = new ArrayList<>(items.size());
        List<List<Long>> parsedItemIds = new ArrayList<>(items.size());
        Set<Long> productIds = new LinkedHashSet<>();
        Set<Long> optionIds = new LinkedHashSet<>();
        Set<Long> itemIds = new LinkedHashSet<>();
        for (CreateOrderProductItem item : items) {
            Long productId = SkuIdParser.parseProductId(item.getSkuId());
            List<Long> opts = SkuIdParser.parseOptionIds(item.getSkuId());
            List<Long> items_ = SkuIdParser.parseItemIds(item.getSkuId());
            parsedOptionIds.add(opts);
            parsedItemIds.add(items_);
            productIds.add(productId);
            optionIds.addAll(opts);
            itemIds.addAll(items_);
        }

        // 查询商品并做存在性校验
        Map<Long, Product> productMap = loadProducts(productIds);

        // 查询商品门店状态（无记录=售罄）
        Map<Long, Integer> productStoreStatusMap = loadProductStoreStatus(productIds, storeId);

        // 查询客制化项目并做存在性校验
        Map<Long, Customization> customizationMap = loadCustomizations(itemIds);

        // 查询客制化选项并做存在性校验
        Map<Long, CustomizationOption> optionMap = loadOptions(optionIds);

        // 查询客制化选项门店状态（无记录=禁用）
        Map<Long, Integer> optionStoreStatusMap = loadOptionStoreStatus(optionIds, storeId);

        // 逐项分类：商品级剔除 → 选项级剔除 → 有效商品汇总
        List<CreateOrderUnavailableProduct> unavailableProducts = new ArrayList<>();
        List<CreateOrderUnavailableCustomization> unavailableOptions = new ArrayList<>();
        Set<Long> reportedProductIds = new LinkedHashSet<>();
        Set<Long> reportedOptionIds = new LinkedHashSet<>();

        int totalQuantity = 0;
        BigDecimal totalPrice = BigDecimal.ZERO;
        List<Long> productIdList = new ArrayList<>(productIds);

        for (int i = 0; i < items.size(); i++) {
            CreateOrderProductItem item = items.get(i);
            List<Long> opts = parsedOptionIds.get(i);
            Long productId = productIdList.get(i);
            Product product = productMap.get(productId);

            // 商品级不可用：全局下架或门店售罄
            if (isProductUnavailable(product, productStoreStatusMap.get(productId))) {
                if (reportedProductIds.add(productId)) {
                    unavailableProducts.add(CreateOrderUnavailableProduct.builder()
                            .id(productId)
                            .name(product.getName())
                            .build());
                }
                continue;
            }

            // 选项级不可用：所属客制化项目非激活、选项全局禁用或门店禁用
            List<CreateOrderUnavailableCustomization> badOptions = new ArrayList<>();
            List<Long> itemIdsForItem = parsedItemIds.get(i);
            for (int j = 0; j < opts.size(); j++) {
                Long optionId = opts.get(j);
                Long itemId = itemIdsForItem.get(j);
                CustomizationOption option = optionMap.get(optionId);
                boolean itemUnavailable = isCustomizationUnavailable(customizationMap.get(itemId));
                boolean optionUnavailable = isOptionUnavailable(option, optionStoreStatusMap.get(optionId));
                if (itemUnavailable || optionUnavailable) {
                    if (reportedOptionIds.add(optionId)) {
                        badOptions.add(CreateOrderUnavailableCustomization.builder()
                                .optionId(optionId)
                                .optionName(option.getName())
                                .productId(productId)
                                .productName(product.getName())
                                .itemId(itemId)
                                .itemName(customizationMap.get(itemId).getName())
                                .build());
                    }
                }
            }
            if (!badOptions.isEmpty()) {
                unavailableOptions.addAll(badOptions);
                continue;
            }

            // 有效商品：累加数量与金额（(商品单价 + 选项加价之和) × 数量）
            BigDecimal unitPrice = nullToZero(product.getPrice());
            for (Long optionId : opts) {
                unitPrice = unitPrice.add(nullToZero(optionMap.get(optionId).getPrice()));
            }
            int quantity = item.getQuantity();
            totalQuantity += quantity;
            totalPrice = totalPrice.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        }

        return CalculateOrderResponse.builder()
                .unavailable(CreateOrderUnavailable.builder()
                        .products(unavailableProducts)
                        .customization(unavailableOptions)
                        .build())
                .totalQuantity(totalQuantity)
                .totalPrice(totalPrice.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    /**
     * 批量查询商品并校验存在性，缺失则抛出商品不存在异常。
     */
    private Map<Long, Product> loadProducts(Set<Long> productIds) {
        List<Product> products = productMapper.selectByIds(new ArrayList<>(productIds));
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));
        List<Long> notFound = productIds.stream()
                .filter(id -> !productMap.containsKey(id))
                .toList();
        if (!notFound.isEmpty()) {
            throw new BizError(OrderErrorCode.PRODUCT_NOT_FOUND, "商品 " + join(notFound) + " 不存在");
        }
        return productMap;
    }

    /**
     * 查询商品在指定门店的库存状态，无记录视为售罄。
     */
    private Map<Long, Integer> loadProductStoreStatus(Set<Long> productIds, Long storeId) {
        Map<Long, Integer> map = new HashMap<>();
        if (productIds.isEmpty()) {
            return map;
        }
        productStoreStatusMapper.selectByProductIdsAndStoreId(new ArrayList<>(productIds), storeId)
                .forEach(s -> map.put(s.getProductId(), s.getStatus()));
        return map;
    }

    /**
     * 批量查询客制化项目并校验存在性，缺失则抛出异常。
     */
    private Map<Long, Customization> loadCustomizations(Set<Long> itemIds) {
        Map<Long, Customization> customizationMap = new HashMap<>();
        if (itemIds.isEmpty()) {
            return customizationMap;
        }
        customizationMapper.selectByIds(new ArrayList<>(itemIds))
                .forEach(c -> customizationMap.put(c.getId(), c));
        List<Long> notFound = itemIds.stream()
                .filter(id -> !customizationMap.containsKey(id))
                .toList();
        if (!notFound.isEmpty()) {
            throw new BizError(OrderErrorCode.CUSTOMIZATION_NOT_FOUND,
                    "客制化项目 " + join(notFound) + " 不存在");
        }
        return customizationMap;
    }

    /**
     * 批量查询客制化选项并校验存在性，缺失则抛出异常。
     */
    private Map<Long, CustomizationOption> loadOptions(Set<Long> optionIds) {
        Map<Long, CustomizationOption> optionMap = new HashMap<>();
        if (optionIds.isEmpty()) {
            return optionMap;
        }
        customizationOptionMapper.selectByIds(new ArrayList<>(optionIds))
                .forEach(o -> optionMap.put(o.getId(), o));
        List<Long> notFound = optionIds.stream()
                .filter(id -> !optionMap.containsKey(id))
                .toList();
        if (!notFound.isEmpty()) {
            throw new BizError(OrderErrorCode.CUSTOMIZATION_OPTION_NOT_FOUND,
                    "客制化选项 " + join(notFound) + " 不存在");
        }
        return optionMap;
    }

    /**
     * 查询客制化选项在指定门店的状态，无记录视为禁用。
     */
    private Map<Long, Integer> loadOptionStoreStatus(Set<Long> optionIds, Long storeId) {
        Map<Long, Integer> map = new HashMap<>();
        if (optionIds.isEmpty()) {
            return map;
        }
        customizationOptionStoreStatusMapper.selectByOptionIdsAndStoreId(new ArrayList<>(optionIds), storeId)
                .forEach(s -> map.put(s.getCustomizationOptionId(), s.getStatus()));
        return map;
    }

    /**
     * 判断商品是否不可用：全局未上架或门店售罄即不可用。
     */
    private boolean isProductUnavailable(Product product, Integer storeStatus) {
        boolean globalOffShelf = product.getStatus() == null
                || product.getStatus() != ProductGlobalStatus.ON_SHELF.getCode();
        boolean storeSoldOut = storeStatus == null
                || storeStatus != ProductStoreStatusEnum.AVAILABLE.getCode();
        return globalOffShelf || storeSoldOut;
    }

    /**
     * 判断客制化选项是否不可用：全局禁用或门店禁用即不可用。
     */
    private boolean isOptionUnavailable(CustomizationOption option, Integer storeStatus) {
        boolean globalDisabled = option.getStatus() == null
                || option.getStatus() != CustomizationOptionGlobalStatus.ACTIVE.getCode();
        boolean storeDisabled = storeStatus == null
                || storeStatus != CustomizationOptionGlobalStatus.ACTIVE.getCode();
        return globalDisabled || storeDisabled;
    }

    /**
     * 判断客制化项目是否不可用：空或非激活状态即不可用。
     */
    private boolean isCustomizationUnavailable(Customization customization) {
        return customization == null
                || customization.getStatus() == null
                || customization.getStatus() != CustomizationStatus.ACTIVE.getCode();
    }

    /**
     * 将 null 金额转换为 0，避免空指针。
     */
    private static BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 将 ID 列表以顿号拼接为字符串，用于异常信息展示。
     */
    private static String join(List<Long> ids) {
        return ids.stream().map(String::valueOf).collect(Collectors.joining("、"));
    }
}
