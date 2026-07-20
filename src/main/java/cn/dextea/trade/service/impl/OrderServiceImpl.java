package cn.dextea.trade.service.impl;

import cn.dextea.trade.common.BizError;
import cn.dextea.trade.dto.CreateOrderRequest;
import cn.dextea.trade.dto.OrderCalculateResponse;
import cn.dextea.trade.dto.OrderCalculateUnavailable;
import cn.dextea.trade.dto.OrderProductItem;
import cn.dextea.trade.dto.UnavailableCustomizationOption;
import cn.dextea.trade.dto.UnavailableProduct;
import cn.dextea.trade.entity.CustomizationOption;
import cn.dextea.trade.entity.Product;
import cn.dextea.trade.entity.enums.CustomizationOptionGlobalStatus;
import cn.dextea.trade.entity.enums.ProductGlobalStatus;
import cn.dextea.trade.entity.enums.ProductStoreStatus;
import cn.dextea.trade.error.OrderErrorCode;
import cn.dextea.trade.mapper.CustomizationOptionMapper;
import cn.dextea.trade.mapper.CustomizationOptionStoreStatusMapper;
import cn.dextea.trade.mapper.ProductMapper;
import cn.dextea.trade.mapper.ProductStoreStatusMapper;
import cn.dextea.trade.service.OrderService;
import cn.dextea.trade.util.SkuIdParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 订单计算服务实现。
 *
 * <p>计算流程：解析 skuId 提取客制化选项 → 批量查询商品/门店状态/选项/选项门店状态
 * → 存在性校验（缺失抛业务异常）→ 剔除不可用商品与含不可用选项的商品 → 汇总有效商品金额。</p>
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final ProductMapper productMapper;
    private final ProductStoreStatusMapper productStoreStatusMapper;
    private final CustomizationOptionMapper customizationOptionMapper;
    private final CustomizationOptionStoreStatusMapper customizationOptionStoreStatusMapper;

    @Override
    public OrderCalculateResponse calculate(CreateOrderRequest request) {
        List<OrderProductItem> items = request == null ? null : request.getProducts();
        if (items == null || items.isEmpty()) {
            return emptyResponse();
        }
        Long storeId = request.getStoreId();

        // 1. 解析 skuId，收集商品ID与客制化选项ID
        List<List<Long>> parsedOptionIds = new ArrayList<>(items.size());
        Set<Long> productIds = new LinkedHashSet<>();
        Set<Long> optionIds = new LinkedHashSet<>();
        for (OrderProductItem item : items) {
            if (item.getId() == null) {
                throw new BizError(OrderErrorCode.SKU_INVALID, "商品ID不能为空");
            }
            List<Long> opts = SkuIdParser.parseOptionIds(item.getSkuId());
            parsedOptionIds.add(opts);
            productIds.add(item.getId());
            optionIds.addAll(opts);
        }

        // 2. 查询商品并做存在性校验
        Map<Long, Product> productMap = loadProducts(productIds);

        // 3. 查询商品门店状态（无记录=售罄）
        Map<Long, Integer> productStoreStatusMap = loadProductStoreStatus(productIds, storeId);

        // 4. 查询客制化选项并做存在性校验
        Map<Long, CustomizationOption> optionMap = loadOptions(optionIds);

        // 5. 查询客制化选项门店状态（无记录=禁用）
        Map<Long, Integer> optionStoreStatusMap = loadOptionStoreStatus(optionIds, storeId);

        // 6. 逐项分类：商品级剔除 → 选项级剔除 → 有效商品汇总
        List<UnavailableProduct> unavailableProducts = new ArrayList<>();
        List<UnavailableCustomizationOption> unavailableOptions = new ArrayList<>();
        Set<Long> reportedProductIds = new LinkedHashSet<>();
        Set<Long> reportedOptionIds = new LinkedHashSet<>();

        int totalQuantity = 0;
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (int i = 0; i < items.size(); i++) {
            OrderProductItem item = items.get(i);
            List<Long> opts = parsedOptionIds.get(i);
            Long productId = item.getId();
            Product product = productMap.get(productId);

            // 商品级不可用：全局下架 或 门店售罄（含无记录）
            if (isProductUnavailable(product, productStoreStatusMap.get(productId))) {
                if (reportedProductIds.add(productId)) {
                    unavailableProducts.add(UnavailableProduct.builder()
                            .id(productId)
                            .name(product.getName())
                            .build());
                }
                continue;
            }

            // 选项级不可用：全局禁用 或 门店禁用（含无记录）
            List<UnavailableCustomizationOption> badOptions = new ArrayList<>();
            for (Long optionId : opts) {
                CustomizationOption option = optionMap.get(optionId);
                if (isOptionUnavailable(option, optionStoreStatusMap.get(optionId))) {
                    if (reportedOptionIds.add(optionId)) {
                        badOptions.add(UnavailableCustomizationOption.builder()
                                .optionId(optionId)
                                .optionName(option.getName())
                                .productId(productId)
                                .productName(product.getName())
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
            int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
            totalQuantity += quantity;
            totalPrice = totalPrice.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        }

        return OrderCalculateResponse.builder()
                .unavailable(OrderCalculateUnavailable.builder()
                        .products(unavailableProducts)
                        .customizationOptions(unavailableOptions)
                        .build())
                .totalQuantity(totalQuantity)
                .totalPrice(totalPrice.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

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

    private Map<Long, Integer> loadProductStoreStatus(Set<Long> productIds, Long storeId) {
        Map<Long, Integer> map = new HashMap<>();
        if (productIds.isEmpty()) {
            return map;
        }
        productStoreStatusMapper.selectByProductIdsAndStoreId(new ArrayList<>(productIds), storeId)
                .forEach(s -> map.put(s.getProductId(), s.getStatus()));
        return map;
    }

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

    private Map<Long, Integer> loadOptionStoreStatus(Set<Long> optionIds, Long storeId) {
        Map<Long, Integer> map = new HashMap<>();
        if (optionIds.isEmpty()) {
            return map;
        }
        customizationOptionStoreStatusMapper.selectByOptionIdsAndStoreId(new ArrayList<>(optionIds), storeId)
                .forEach(s -> map.put(s.getCustomizationOptionId(), s.getStatus()));
        return map;
    }

    /** 商品不可用：全局下架，或门店售罄（无记录默认售罄）。 */
    private boolean isProductUnavailable(Product product, Integer storeStatus) {
        boolean globalOffShelf = product.getStatus() == null
                || product.getStatus() != ProductGlobalStatus.ON_SHELF.getCode();
        boolean storeSoldOut = storeStatus == null
                || storeStatus != ProductStoreStatus.AVAILABLE.getCode();
        return globalOffShelf || storeSoldOut;
    }

    /** 客制化选项不可用：全局禁用，或门店禁用（无记录默认禁用）。 */
    private boolean isOptionUnavailable(CustomizationOption option, Integer storeStatus) {
        boolean globalDisabled = option.getStatus() == null
                || option.getStatus() != CustomizationOptionGlobalStatus.ACTIVE.getCode();
        boolean storeDisabled = storeStatus == null
                || storeStatus != CustomizationOptionGlobalStatus.ACTIVE.getCode();
        return globalDisabled || storeDisabled;
    }

    private OrderCalculateResponse emptyResponse() {
        return OrderCalculateResponse.builder()
                .unavailable(OrderCalculateUnavailable.builder()
                        .products(new ArrayList<>())
                        .customizationOptions(new ArrayList<>())
                        .build())
                .totalQuantity(0)
                .totalPrice(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    private static BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String join(List<Long> ids) {
        return ids.stream().map(String::valueOf).collect(Collectors.joining("、"));
    }
}
