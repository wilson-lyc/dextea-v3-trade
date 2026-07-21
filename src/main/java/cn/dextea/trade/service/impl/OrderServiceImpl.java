package cn.dextea.trade.service.impl;

import cn.dextea.trade.exception.BizError;
import cn.dextea.trade.dto.CreateOrderRequest;
import cn.dextea.trade.dto.PreBuildOrderResponse;
import cn.dextea.trade.dto.CreateOrderResponse;
import cn.dextea.trade.dto.CreateOrderUnavailable;
import cn.dextea.trade.dto.CreateOrderProductItem;
import cn.dextea.trade.dto.CreateOrderUnavailableCustomization;
import cn.dextea.trade.dto.CreateOrderUnavailableProduct;
import cn.dextea.trade.dto.CreateAlipayTradeRequest;
import cn.dextea.trade.config.AlipaySdkConfig;
import cn.dextea.trade.entity.Customization;
import cn.dextea.trade.entity.CustomizationOption;
import cn.dextea.trade.entity.Customer;
import cn.dextea.trade.entity.Order;
import cn.dextea.trade.entity.Product;
import cn.dextea.trade.entity.Store;
import cn.dextea.trade.entity.enums.CustomizationOptionGlobalStatus;
import cn.dextea.trade.entity.enums.CustomizationStatus;
import cn.dextea.trade.entity.enums.OrderStatus;
import cn.dextea.trade.entity.enums.PayMethod;
import cn.dextea.trade.entity.enums.ProductGlobalStatus;
import cn.dextea.trade.entity.enums.ProductStoreStatusEnum;
import cn.dextea.trade.error.OrderErrorCode;
import cn.dextea.trade.mapper.CustomerMapper;
import cn.dextea.trade.mapper.CustomizationMapper;
import cn.dextea.trade.mapper.CustomizationOptionMapper;
import cn.dextea.trade.mapper.CustomizationOptionStoreStatusMapper;
import cn.dextea.trade.mapper.OrderMapper;
import cn.dextea.trade.mapper.ProductImageMapper;
import cn.dextea.trade.mapper.ProductMapper;
import cn.dextea.trade.mapper.ProductStoreStatusMapper;
import cn.dextea.trade.mapper.StoreMapper;
import cn.dextea.trade.service.AlipayService;
import cn.dextea.trade.service.OrderService;
import cn.dextea.trade.util.SkuIdParser;
import lombok.RequiredArgsConstructor;
import me.ahoo.cosid.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Duration;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final ProductImageMapper productImageMapper;
    private final ProductStoreStatusMapper productStoreStatusMapper;
    private final CustomizationMapper customizationMapper;
    private final CustomizationOptionMapper customizationOptionMapper;
    private final CustomizationOptionStoreStatusMapper customizationOptionStoreStatusMapper;
    private final StoreMapper storeMapper;
    private final CustomerMapper customerMapper;
    private final OrderMapper orderMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final IdGenerator idGenerator;
    private final AlipayService alipayService;
    private final AlipaySdkConfig alipayConfig;

    private static final String IDEMPOTENCY_KEY_PREFIX = "idem:order:";
    private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Override
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        String idempotencyKey = request.getIdempotencyKey();
        String redisKey = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;

        // 1. Redis 快校验：命中说明已创建过，直接返回首次结果（真正幂等，不报错）
        CreateOrderResponse cached = getCachedResult(redisKey);
        if (cached != null) {
            return cached;
        }

        // 2. 预构建：校验数据合法性并计价
        PreBuildOrderResponse summary = preBuild(request);

        // 存在不可用项时不创建订单记录，也不占用幂等键，允许修正购物车后正常重试
        if (hasUnavailable(summary.getUnavailable())) {
            return toCreateOrderResponse(summary, null);
        }

        // 3. 落库：MySQL 唯一索引兜底，真正保证同幂等键只创建一个订单
        Order order = Order.builder()
                .orderNo(idGenerator.generate()) // 订单号在代码中显式生成，作为支付宝 out_trade_no
                .tradeNo(null) // 支付宝交易号在调用 alipay.trade.create 后回填
                .idempotencyKey(idempotencyKey)
                .customerId(request.getCustomerId())
                .storeId(request.getStoreId())
                .status(OrderStatus.PENDING.getCode())
                .payMethod(request.getPlatform().getPayMethod().getCode())
                .totalPrice(summary.getTotalPrice())
                .totalQuantity(summary.getTotalQuantity())
                .build();

        try {
            orderMapper.insert(order);
        } catch (DuplicateKeyException e) {
            // Redis 快校验过期但 DB 已有记录：查回已存在订单，复用其订单号与交易号
            Order existing = orderMapper.selectByIdempotencyKey(idempotencyKey);
            if (existing == null) {
                throw new BizError(OrderErrorCode.ORDER_CREATE_FAILED, "订单创建冲突，请稍后重试");
            }
            order = existing;
        }

        // 4. 支付宝支付：创建交易并回填 trade_no。
        //    幂等保证：已存在且已生成 trade_no 则跳过；否则用订单号(out_trade_no)创建，失败不缓存可重试。
        if (PayMethod.ALIPAY.getCode().equals(order.getPayMethod()) && order.getTradeNo() == null) {
            Customer customer = customerMapper.selectById(order.getCustomerId());
            if (customer == null || customer.getAlipayOpenId() == null) {
                throw new BizError(OrderErrorCode.ALIPAY_BUYER_NOT_BOUND, "顾客未绑定支付宝，无法创建支付");
            }
            CreateAlipayTradeRequest alipayRequest = CreateAlipayTradeRequest.builder()
                    .orderNo(order.getOrderNo())
                    .totalPrice(order.getTotalPrice())
                    .subject(alipayConfig.getSubject())
                    .customerAlipayOpenId(customer.getAlipayOpenId())
                    .build();
            String tradeNo = alipayService.createTrade(alipayRequest);
            order.setTradeNo(tradeNo);
            orderMapper.updateTradeNo(order.getId(), tradeNo);
        }

        // 5. 缓存首次结果，后续携带相同幂等键的请求直接返回，无需再查 DB
        CreateOrderResponse response = toCreateOrderResponse(summary, order);
        cacheResult(redisKey, response);
        return response;
    }

    /**
     * 统一将预构建结果映射为创建订单响应。
     * order 为 null 表示未落库（如存在不可用项），此时订单标识字段（id/orderNo/tradeNo）置空。
     */
    private CreateOrderResponse toCreateOrderResponse(PreBuildOrderResponse summary, Order order) {
        return CreateOrderResponse.builder()
                .id(order != null ? order.getId() : null)
                .orderNo(order != null ? order.getOrderNo() : null)
                .tradeNo(order != null ? order.getTradeNo() : null)
                .totalQuantity(summary.getTotalQuantity())
                .totalPrice(summary.getTotalPrice())
                .unavailable(summary.getUnavailable())
                .products(summary.getProducts())
                .build();
    }

    /**
     * 读取幂等缓存结果。Redis 不可用或反序列化失败时降级为 null，交由 MySQL 唯一索引兜底。
     */
    private CreateOrderResponse getCachedResult(String redisKey) {
        try {
            String json = redisTemplate.opsForValue().get(redisKey);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, CreateOrderResponse.class);
        } catch (RuntimeException | IOException e) {
            log.warn("Redis 读取幂等结果失败，降级至 MySQL 唯一索引: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 缓存首次创建结果（JSON，含订单 id/orderNo/数量/金额），TTL 覆盖正常重试窗口。
     * Redis 不可用时忽略，不影响下单。
     */
    private void cacheResult(String redisKey, CreateOrderResponse response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(redisKey, json, IDEMPOTENCY_TTL);
        } catch (RuntimeException | IOException e) {
            log.warn("Redis 缓存幂等结果失败（不影响下单）: {}", e.getMessage());
        }
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
     * 预构建订单：对外暴露的预构建入口，直接复用内部预构建逻辑，返回可用项汇总与不可用清单。
     */
    @Override
    public PreBuildOrderResponse preBuildOrder(CreateOrderRequest request) {
        return preBuild(request);
    }

    /**
     * 预构建核心逻辑：校验门店与顾客合法性，解析 skuId、校验商品与客制化选项的存在性与门店状态，
     * 剔除不可用项并汇总有效商品的总数量与总金额。
     */
    private PreBuildOrderResponse preBuild(CreateOrderRequest request) {
        // 先校验门店ID与顾客ID合法性，任一不合法直接抛业务异常
        validateStore(request.getStoreId());
        validateCustomer(request.getCustomerId());

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

        // 查询商品封面图（type=1），得到 productId -> coverId 的映射
        Map<Long, Long> productCoverMap = loadCoverIds(productIds);

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
        List<CreateOrderProductItem> availableProducts = new ArrayList<>();
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

            // 选项级不可用：所属客制化项目非激活、选项全局禁用或门店禁用，
            // 以及跨绑定异常（客制化项目不属于当前商品、客制化选项不属于当前客制化项目）
            List<CreateOrderUnavailableCustomization> badOptions = new ArrayList<>();
            List<Long> itemIdsForItem = parsedItemIds.get(i);
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
                    if (reportedOptionIds.add(optionId)) {
                        badOptions.add(CreateOrderUnavailableCustomization.builder()
                                .optionId(optionId)
                                .optionName(option.getName())
                                .productId(productId)
                                .productName(product.getName())
                                .itemId(itemId)
                                .itemName(customization.getName())
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
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            totalPrice = totalPrice.add(subtotal);

            // 校验/计价阶段顺手构建有效商品明细，供落库复用，免去重复查表
            availableProducts.add(CreateOrderProductItem.builder()
                    .skuId(item.getSkuId())
                    .quantity(quantity)
                    .productId(productId)
                    .productName(product.getName())
                    .coverId(productCoverMap.get(productId))
                    .unitPrice(unitPrice.setScale(2, RoundingMode.HALF_UP))
                    .subtotal(subtotal.setScale(2, RoundingMode.HALF_UP))
                    .build());
        }

        return PreBuildOrderResponse.builder()
                .unavailable(CreateOrderUnavailable.builder()
                        .products(unavailableProducts)
                        .customization(unavailableOptions)
                        .build())
                .products(availableProducts)
                .totalQuantity(totalQuantity)
                .totalPrice(totalPrice.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    /**
     * 校验门店ID合法性，不存在则抛出业务异常。
     */
    private void validateStore(Long storeId) {
        Store store = storeMapper.selectById(storeId);
        if (store == null) {
            throw new BizError(OrderErrorCode.STORE_ID_INVALID, "门店ID错误: " + storeId);
        }
    }

    /**
     * 校验顾客ID合法性，不存在则抛出业务异常。
     */
    private void validateCustomer(Long customerId) {
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null) {
            throw new BizError(OrderErrorCode.CUSTOMER_ID_INVALID, "顾客ID错误: " + customerId);
        }
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
            throw new BizError(OrderErrorCode.PRODUCT_NOT_FOUND, "商品ID错误: " + join(notFound));
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
     * 批量查询商品封面图（type=1），返回 productId -> coverId(即 gallery.id) 映射。
     * 封面图至多 1 张，若存在多张则以 sort、created_at、image_id 升序的第一张为准（查询已按此排序，putIfAbsent 保留首条）。
     */
    private Map<Long, Long> loadCoverIds(Set<Long> productIds) {
        Map<Long, Long> map = new HashMap<>();
        if (productIds.isEmpty()) {
            return map;
        }
        productImageMapper.selectCoverImagesByProductIds(new ArrayList<>(productIds))
                .forEach(pi -> map.putIfAbsent(pi.getProductId(), pi.getImageId()));
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
                    "客制化项目ID错误: " + join(notFound));
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
                    "客制化选项ID错误: " + join(notFound));
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
