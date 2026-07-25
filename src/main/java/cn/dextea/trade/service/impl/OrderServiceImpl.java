package cn.dextea.trade.service.impl;

import cn.dextea.trade.exception.BizError;
import cn.dextea.trade.model.AbstractOrderRequest;
import cn.dextea.trade.model.CreateOrderRequest;
import cn.dextea.trade.model.PreBuildOrderRequest;
import cn.dextea.trade.model.PreBuildOrderResponse;
import cn.dextea.trade.model.CreateOrderResponse;
import cn.dextea.trade.model.CreateOrderUnavailable;
import cn.dextea.trade.model.CreateOrderProductItem;
import cn.dextea.trade.model.CreateOrderUnavailableCustomization;
import cn.dextea.trade.model.CreateOrderUnavailableProduct;
import cn.dextea.trade.model.CreateAlipayTradeRequest;
import cn.dextea.trade.model.OrderSummary;
import cn.dextea.trade.model.OrderDetailItem;
import cn.dextea.trade.model.OrderDetailResponse;
import cn.dextea.trade.model.StoreInfo;
import cn.dextea.trade.config.AlipayClientConfig;
import cn.dextea.trade.entity.Customization;
import cn.dextea.trade.entity.CustomizationOption;
import cn.dextea.trade.entity.Customer;
import cn.dextea.trade.entity.Gallery;
import cn.dextea.trade.entity.Order;
import cn.dextea.trade.entity.OrderItem;
import cn.dextea.trade.entity.Product;
import cn.dextea.trade.entity.Store;
import cn.dextea.trade.enums.CustomizationOptionGlobalStatusEnum;
import cn.dextea.trade.enums.CustomizationStatusEnum;
import cn.dextea.trade.enums.DiningMethodEnum;
import cn.dextea.trade.enums.MakingStatusEnum;
import cn.dextea.trade.enums.TradeStatusEnum;
import cn.dextea.trade.enums.PayMethodEnum;
import cn.dextea.trade.enums.PlatformEnum;
import cn.dextea.trade.enums.ProductGlobalStatusEnum;
import cn.dextea.trade.enums.ProductStoreStatusEnum;
import cn.dextea.trade.enums.StoreStatusEnum;
import cn.dextea.trade.enums.CustomerStatusEnum;
import cn.dextea.trade.error.OrderErrorCode;
import cn.dextea.trade.mapper.CustomerMapper;
import cn.dextea.trade.mapper.CustomizationMapper;
import cn.dextea.trade.mapper.CustomizationOptionMapper;
import cn.dextea.trade.mapper.CustomizationOptionStoreStatusMapper;
import cn.dextea.trade.mapper.GalleryMapper;
import cn.dextea.trade.mapper.OrderItemMapper;
import cn.dextea.trade.mapper.OrderMapper;
import cn.dextea.trade.mapper.ProductImageMapper;
import cn.dextea.trade.mapper.ProductMapper;
import cn.dextea.trade.mapper.ProductStoreStatusMapper;
import cn.dextea.trade.mapper.StoreMapper;
import cn.dextea.trade.service.AlipayService;
import cn.dextea.trade.service.OrderService;
import cn.dextea.trade.util.SkuIdParser;
import lombok.RequiredArgsConstructor;
import me.ahoo.cosid.provider.IdGeneratorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final GalleryMapper galleryMapper;
    private final ProductStoreStatusMapper productStoreStatusMapper;
    private final CustomizationMapper customizationMapper;
    private final CustomizationOptionMapper customizationOptionMapper;
    private final CustomizationOptionStoreStatusMapper customizationOptionStoreStatusMapper;
    private final StoreMapper storeMapper;
    private final CustomerMapper customerMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final IdGeneratorProvider idGeneratorProvider;
    private final AlipayService alipayService;
    private final AlipayClientConfig alipayConfig;

    private static final String IDEMPOTENCY_KEY_PREFIX = "dextea:order:idem:";
    private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);
    /**
     * 订单号生成器名称，对应 cosid.snowflake.provider.order。
     * 后续如有更多 ID 类型（如退款单号、流水号），在 YAML 中追加命名生成器并改用对应名称即可。
     */
    private static final String ORDER_ID_GENERATOR = "order";
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    /**
     * 预构建订单（对外入口）
     *
     * @param request 创建订单请求
     * @return 预构建订单响应
     */
    @Override
    public PreBuildOrderResponse preBuildOrder(PreBuildOrderRequest request) {
        return preBuild(request);
    }

    /**
     * 创建订单
     *
     * @param request 创建订单请求
     * @return 创建订单响应
     */
    @Override
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        // 0. 支付方式拦截：微信支付暂未实现，识别到后直接抛业务异常
        if (PlatformEnum.WEIXIN.equals(request.getPlatform())) {
            throw new BizError(OrderErrorCode.PAY_PLATFORM_NOT_SUPPORTED, "微信支付暂不支持");
        }

        // 校验用餐方式合法性
        DiningMethodEnum diningMethod = DiningMethodEnum.of(request.getDiningMethod());
        if (diningMethod == null) {
            throw new BizError(OrderErrorCode.DINING_METHOD_INVALID, "用餐方式错误: " + request.getDiningMethod());
        }

        String idempotencyKey = request.getIdempotencyKey();
        String redisKey = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;

        // 1. Redis 快校验：命中说明已创建过，直接返回结果
        CreateOrderResponse cached = getCachedResult(redisKey);
        if (cached != null) {
            return cached;
        }

        // 2. 构建订单：校验数据合法性并计价
        PreBuildOrderResponse summary = preBuild(request);

        // 门店或顾客不可用时直接拒绝下单，保证不会为无效主体创建订单
        if (!Boolean.TRUE.equals(summary.getStoreAvailable())) {
            throw new BizError(OrderErrorCode.STORE_NOT_OPEN, "门店不可下单: " + request.getStoreId());
        }
        if (!Boolean.TRUE.equals(summary.getCustomerAvailable())) {
            throw new BizError(OrderErrorCode.CUSTOMER_NOT_ACTIVE, "顾客不可下单: " + request.getCustomerId());
        }

        // 存在不可用项时不创建订单记录，也不占用幂等键，允许修正购物车后正常重试
        if (hasUnavailable(summary.getUnavailable())) {
            return toCreateOrderResponse(summary, null);
        }

        // 3. 落库：MySQL 唯一索引兜底，真正保证同幂等键只创建一个订单
        Order order = Order.builder()
                .orderNo(String.valueOf(idGeneratorProvider.getRequired(ORDER_ID_GENERATOR).generate())) // 订单号在代码中显式生成，作为支付宝 out_trade_no（generate() 返回原始 long，转十进制字符串）
                .tradeNo(null) // 支付宝交易号在调用 alipay.trade.create 后回填
                .idempotencyKey(idempotencyKey)
                .customerId(request.getCustomerId())
                .storeId(request.getStoreId())
                .tradeStatus(TradeStatusEnum.TRADE_WAIT_PAY.getCode())
                .makingStatus(MakingStatusEnum.MAKING_WAIT.getCode())
                .payMethod(request.getPlatform().getPayMethod().getCode())
                .diningMethod(diningMethod.getCode())
                .note(request.getNote())
                .totalPrice(summary.getTotalPrice())
                .totalQuantity(summary.getTotalQuantity())
                .build();

        boolean newlyCreated = true;
        try {
            orderMapper.insert(order);
        } catch (DuplicateKeyException e) {
            // Redis 快校验过期但 DB 已有记录：查回已存在订单，复用其订单号与交易号
            Order existing = orderMapper.selectByIdempotencyKey(idempotencyKey);
            if (existing == null) {
                throw new BizError(OrderErrorCode.ORDER_CREATE_FAILED, "订单创建冲突，请稍后重试");
            }
            order = existing;
            newlyCreated = false;
        }

        // 3.1 仅首次创建时写入订单明细，重复请求命中已存在订单时跳过，避免重复插入
        if (newlyCreated) {
            List<OrderItem> orderItems = buildOrderItems(order.getId(), summary.getProducts());
            if (!orderItems.isEmpty()) {
                orderItemMapper.batchInsert(orderItems);
            }
        }

        // 4. 支付宝支付：创建交易并回填 trade_no。
        //    幂等保证：已存在且已生成 trade_no 则跳过；否则用订单号(out_trade_no)创建，失败不缓存可重试。
        if (Integer.valueOf(PayMethodEnum.ALIPAY.getCode()).equals(order.getPayMethod()) && order.getTradeNo() == null) {
            Customer customer = customerMapper.selectById(order.getCustomerId());
            if (customer == null || customer.getAlipayOpenId() == null) {
                throw new BizError(OrderErrorCode.ALIPAY_BUYER_NOT_BOUND, "顾客未绑定支付宝，无法创建支付");
            }
            CreateAlipayTradeRequest alipayRequest = CreateAlipayTradeRequest.builder()
                    .orderNo(order.getOrderNo())
                    .totalPrice(order.getTotalPrice())
                    .subject(alipayConfig.getSubject())
                    .appId(alipayConfig.getAppId())
                    .productCode(alipayConfig.getProductCode())
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
     * 获取用户近3个月内的订单列表
     *
     * @param customerId 用户ID
     * @return 订单概要列表（无订单则空列表）
     */
    @Override
    public List<OrderSummary> getOrdersByCustomer(Long customerId) {
        // 1. 查询近3个月内的订单，按下单时间倒序
        LocalDateTime since = LocalDateTime.now().minusMonths(3);
        List<Order> orders = orderMapper.selectByCustomerIdAndCreatedAtAfter(customerId, since);
        if (orders.isEmpty()) {
            return List.of();
        }

        // 2. 批量查询订单明细中的封面图ID，并按订单分组
        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        Map<Long, List<Long>> coverIdsByOrder = new HashMap<>();
        Set<Long> allCoverIds = new LinkedHashSet<>();
        for (OrderItem item : orderItemMapper.selectByOrderIds(orderIds)) {
            Long coverId = item.getCoverId();
            coverIdsByOrder
                    .computeIfAbsent(item.getOrderId(), k -> new ArrayList<>())
                    .add(coverId);
            if (coverId != null) {
                allCoverIds.add(coverId);
            }
        }

        // 3. 批量解析封面图URL（去重后一次查询）
        Map<Long, String> coverUrlMap = new HashMap<>();
        if (!allCoverIds.isEmpty()) {
            coverUrlMap.putAll(galleryMapper.selectByIds(new ArrayList<>(allCoverIds)).stream()
                    .collect(Collectors.toMap(Gallery::getId, Gallery::getUrl, (a, b) -> a)));
        }

        // 4. 组装响应：每个订单解析门店名称与封面图URL
        List<OrderSummary> result = new ArrayList<>(orders.size());
        for (Order order : orders) {
            List<String> coverUrls = coverIdsByOrder.getOrDefault(order.getId(), List.of()).stream()
                    .filter(Objects::nonNull)
                    .map(coverUrlMap::get)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            Store store = storeMapper.selectById(order.getStoreId());
            result.add(OrderSummary.builder()
                    .storeName(store != null ? store.getName() : null)
                    .orderTime(order.getCreatedAt())
                    .tradeStatus(order.getTradeStatus())
                    .tradeStatusDesc(safeEnumDesc(() -> TradeStatusEnum.of(order.getTradeStatus()).getDescription()))
                    .makingStatus(order.getMakingStatus())
                    .makingStatusDesc(safeEnumDesc(() -> MakingStatusEnum.of(order.getMakingStatus()).getDescription()))
                    .totalPrice(order.getTotalPrice())
                    .totalQuantity(order.getTotalQuantity())
                    .coverUrls(coverUrls)
                    .build());
        }
        return result;
    }

    /**
     * 获取订单详情
     *
     * @param orderId 订单ID（数据库主键）
     * @param customerId 顾客ID（用于归属校验）
     * @return 订单详情响应
     */
    @Override
    public OrderDetailResponse getOrderDetail(Long orderId, Long customerId) {
        // 1. 查询订单，不存在则直接报错
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizError(OrderErrorCode.ORDER_NOT_FOUND, "订单不存在: " + orderId);
        }

        // 2. 归属校验：订单所属顾客必须等于入参顾客ID
        if (!Objects.equals(order.getCustomerId(), customerId)) {
            throw new BizError(OrderErrorCode.ORDER_ACCESS_DENIED, "订单不属于该顾客: " + orderId);
        }

        // 3. 组装门店信息
        StoreInfo storeInfo = null;
        Store store = storeMapper.selectById(order.getStoreId());
        if (store != null) {
            storeInfo = StoreInfo.builder()
                    .id(store.getId())
                    .name(store.getName())
                    .address(store.getAddress())
                    .phone(store.getPhone())
                    .businessHours(store.getBusinessHours())
                    .build();
        }

        // 4. 查询订单明细，并批量解析封面图 URL 与客制化文本
        List<OrderItem> items = orderItemMapper.selectFullByOrderId(orderId);
        Map<Long, String> coverUrlMap = resolveCoverUrls(items);
        Map<Long, String> customizationTextMap = resolveCustomizationTexts(items);

        List<OrderDetailItem> detailItems = items.stream()
                .map(item -> OrderDetailItem.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .skuId(item.getSkuId())
                        .coverUrl(item.getCoverId() != null ? coverUrlMap.get(item.getCoverId()) : null)
                        .customizationText(customizationTextMap.get(item.getId()))
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.<OrderDetailItem>toList());

        // 5. 组装响应，枚举 code 回填可读文案
        return OrderDetailResponse.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .tradeNo(order.getTradeNo())
                .tradeStatus(order.getTradeStatus())
                .tradeStatusDesc(safeEnumDesc(() -> TradeStatusEnum.of(order.getTradeStatus()).getDescription()))
                .makingStatus(order.getMakingStatus())
                .makingStatusDesc(safeEnumDesc(() -> MakingStatusEnum.of(order.getMakingStatus()).getDescription()))
                .totalPrice(order.getTotalPrice())
                .totalQuantity(order.getTotalQuantity())
                .payMethod(order.getPayMethod())
                .payMethodDesc(safeEnumDesc(() -> PayMethodEnum.of(order.getPayMethod()).getDescription()))
                .diningMethod(order.getDiningMethod())
                .diningMethodDesc(safeEnumDesc(() -> DiningMethodEnum.of(order.getDiningMethod()).getDescription()))
                .note(order.getNote())
                .createdAt(order.getCreatedAt())
                .paidAt(order.getPaidAt())
                .refundedAt(order.getRefundedAt())
                .updatedAt(order.getUpdatedAt())
                .store(storeInfo)
                .items(detailItems)
                .build();
    }

    /**
     * 批量解析订单明细的封面图 URL（按 coverId 去重后一次查询 gallery）
     *
     * @param items 订单明细列表
     * @return coverId 到封面图 URL 的映射
     */
    private Map<Long, String> resolveCoverUrls(List<OrderItem> items) {
        Set<Long> coverIds = items.stream()
                .map(OrderItem::getCoverId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (coverIds.isEmpty()) {
            return Map.of();
        }
        return galleryMapper.selectByIds(new ArrayList<>(coverIds)).stream()
                .collect(Collectors.toMap(Gallery::getId, Gallery::getUrl, (a, b) -> a));
    }

    /**
     * 由明细的 skuId 批量解析客制化文本（选项名称以「 / 」拼接）。
     * <p>order_items 仅持久化 skuId，不单独存储客制化文本，故在此由 skuId 还原，无需改动表结构。</p>
     *
     * @param items 订单明细列表
     * @return 订单明细ID到客制化文本的映射（无选项则对应值为 null）
     */
    private Map<Long, String> resolveCustomizationTexts(List<OrderItem> items) {
        Set<Long> optionIds = new LinkedHashSet<>();
        for (OrderItem item : items) {
            if (item.getSkuId() != null) {
                optionIds.addAll(SkuIdParser.parseOptionIds(item.getSkuId()));
            }
        }
        Map<Long, String> nameMap = new HashMap<>();
        if (!optionIds.isEmpty()) {
            for (CustomizationOption opt : customizationOptionMapper.selectByIds(new ArrayList<>(optionIds))) {
                nameMap.put(opt.getId(), opt.getName());
            }
        }
        Map<Long, String> result = new HashMap<>();
        for (OrderItem item : items) {
            if (item.getSkuId() == null) {
                result.put(item.getId(), null);
                continue;
            }
            String text = SkuIdParser.parseOptionIds(item.getSkuId()).stream()
                    .map(nameMap::get)
                    .filter(Objects::nonNull)
                    .filter(n -> !n.isBlank())
                    .collect(Collectors.joining(" / "));
            result.put(item.getId(), text.isEmpty() ? null : text);
        }
        return result;
    }

    /**
     * 安全获取枚举文案：code 未知时返回 null，避免脏数据导致 500
     *
     * @param supplier 枚举文案获取逻辑
     * @return 文案（获取失败返回 null）
     */
    private static String safeEnumDesc(Supplier<String> supplier) {
        try {
            return supplier.get();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 映射创建订单响应
     *
     * @param summary 预构建结果
     * @param order 订单（无则空）
     * @return 创建订单响应
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
                .storeAvailable(summary.getStoreAvailable())
                .customerAvailable(summary.getCustomerAvailable())
                .build();
    }

    /**
     * 由预构建计价结果构建订单明细列表
     *
     * @param orderId 订单ID
     * @param products 预构建的有效商品明细
     * @return 订单明细列表
     */
    private List<OrderItem> buildOrderItems(Long orderId, List<CreateOrderProductItem> products) {
        List<OrderItem> items = new ArrayList<>();
        if (products == null) {
            return items;
        }
        for (CreateOrderProductItem product : products) {
            items.add(OrderItem.builder()
                    .orderId(orderId)
                    .productId(product.getProductId())
                    .skuId(product.getSkuId())
                    .productName(product.getProductName())
                    .coverId(product.getCoverId())
                    .quantity(product.getQuantity())
                    .unitPrice(product.getUnitPrice())
                    .subtotal(product.getSubtotal())
                    .build());
        }
        return items;
    }

    /**
     * 读取幂等缓存结果
     *
     * @param redisKey 幂等缓存键
     * @return 缓存的创建订单响应（无则 null）
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
     * 缓存创建结果
     *
     * @param redisKey 幂等缓存键
     * @param response 创建订单响应
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
     * 判断是否存在不可用项
     *
     * @param unavailable 不可用清单
     * @return 是否存在不可用项
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
     * 预构建订单
     *
     * @param request 创建订单请求
     * @return 预构建订单响应
     */
    private PreBuildOrderResponse preBuild(AbstractOrderRequest request) {
        Long storeId = request.getStoreId();
        Long customerId = request.getCustomerId();
        List<CreateOrderProductItem> items = request.getProducts();

        // 1. 校验门店与顾客可用性
        boolean storeAvailable = isStoreAvailable(storeId);
        boolean customerAvailable = isCustomerAvailable(customerId);
        if (!storeAvailable || !customerAvailable) {
            return PreBuildOrderResponse.builder()
                    .storeAvailable(storeAvailable)
                    .customerAvailable(customerAvailable)
                    .build();
        }

        // 2. 解析 skuId，获取商品/选项/客制化项目 ID
        SkuResolution resolution = resolveSkuIds(items);

        // 3. 批量加载所有关联实体（商品、封面、门店状态、客制化项目、选项）
        LoadedEntities entities = loadAllEntities(resolution, storeId);

        // 4. 逐项分类：商品级剔除 → 选项级剔除 → 有效商品汇总
        List<CreateOrderUnavailableProduct> unavailableProducts = new ArrayList<>();
        List<CreateOrderUnavailableCustomization> unavailableOptions = new ArrayList<>();
        Set<Long> reportedProductIds = new LinkedHashSet<>();
        Set<Long> reportedOptionIds = new LinkedHashSet<>();

        int totalQuantity = 0;
        BigDecimal totalPrice = BigDecimal.ZERO;
        List<CreateOrderProductItem> availableProducts = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            CreateOrderProductItem item = items.get(i);
            List<Long> opts = resolution.optionIds().get(i);
            Long productId = resolution.productIds().get(i);
            Product product = entities.productMap().get(productId);

            // 商品级不可用：全局下架或门店售罄
            Optional<CreateOrderUnavailableProduct> productUnavailable = checkProductAvailability(
                    product, entities.productStoreStatusMap().get(productId), productId);
            if (productUnavailable.isPresent()) {
                if (reportedProductIds.add(productId)) {
                    unavailableProducts.add(productUnavailable.get());
                }
                continue;
            }

            // 选项级不可用：客制化项目非激活、选项禁用、跨绑定异常
            List<CreateOrderUnavailableCustomization> badOptions = checkOptionAvailability(
                    opts, resolution.itemIds().get(i), productId, product,
                    entities.optionMap(), entities.customizationMap(), entities.optionStoreStatusMap());
            if (!badOptions.isEmpty()) {
                for (CreateOrderUnavailableCustomization bad : badOptions) {
                    if (reportedOptionIds.add(bad.getOptionId())) {
                        unavailableOptions.add(bad);
                    }
                }
                continue;
            }

            // 有效商品：计价并构建明细
            PricedItem priced = priceItem(item, product, opts, entities.optionMap(), entities.productCoverMap(), entities.productCoverUrlMap());
            totalQuantity += priced.quantity();
            totalPrice = totalPrice.add(priced.subtotal());
            availableProducts.add(priced.detail());
        }

        return PreBuildOrderResponse.builder()
                .unavailable(CreateOrderUnavailable.builder()
                        .products(unavailableProducts)
                        .customization(unavailableOptions)
                        .build())
                .products(availableProducts)
                .storeAvailable(true)
                .customerAvailable(true)
                .totalQuantity(totalQuantity)
                .totalPrice(totalPrice.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    /**
     * 解析商品 skuId
     *
     * @param items 商品项列表
     * @return skuId 解析结果
     */
    private SkuResolution resolveSkuIds(List<CreateOrderProductItem> items) {
        List<Long> productIds = new ArrayList<>(items.size());
        List<List<Long>> optionIds = new ArrayList<>(items.size());
        List<List<Long>> itemIds = new ArrayList<>(items.size());
        Set<Long> allProductIds = new LinkedHashSet<>();
        Set<Long> allOptionIds = new LinkedHashSet<>();
        Set<Long> allItemIds = new LinkedHashSet<>();
        for (CreateOrderProductItem item : items) {
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

    /**
     * 批量加载关联实体
     *
     * @param resolution skuId 解析结果
     * @param storeId 门店ID
     * @return 关联实体上下文
     */
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

    /**
     * 商品级可用性校验
     *
     * @param product 商品
     * @param storeStatus 门店库存状态
     * @param productId 商品ID
     * @return 不可用商品（无则空）
     */
    private Optional<CreateOrderUnavailableProduct> checkProductAvailability(
            Product product, Integer storeStatus, Long productId) {
        if (isProductUnavailable(product, storeStatus)) {
            return Optional.of(CreateOrderUnavailableProduct.builder()
                    .id(productId)
                    .name(product.getName())
                    .build());
        }
        return Optional.empty();
    }

    /**
     * 选项级可用性校验
     *
     * @param opts 选项ID列表
     * @param itemIdsForItem 客制化项目ID列表
     * @param productId 商品ID
     * @param product 商品
     * @param optionMap 选项映射
     * @param customizationMap 客制化项目映射
     * @param optionStoreStatusMap 选项门店状态映射
     * @return 不可用客制化选项列表
     */
    private List<CreateOrderUnavailableCustomization> checkOptionAvailability(
            List<Long> opts, List<Long> itemIdsForItem, Long productId, Product product,
            Map<Long, CustomizationOption> optionMap, Map<Long, Customization> customizationMap,
            Map<Long, Integer> optionStoreStatusMap) {
        List<CreateOrderUnavailableCustomization> badOptions = new ArrayList<>();
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
        return badOptions;
    }

    /**
     * 有效商品计价
     *
     * @param item 商品项
     * @param product 商品
     * @param opts 选项ID列表
     * @param optionMap 选项映射
     * @param productCoverMap 商品封面映射
     * @return 计价结果
     */
    private PricedItem priceItem(CreateOrderProductItem item, Product product, List<Long> opts,
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
        CreateOrderProductItem detail = CreateOrderProductItem.builder()
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

    /**
     * 校验门店
     *
     * @param storeId 门店ID
     */
    private boolean isStoreAvailable(Long storeId) {
        Store store = storeMapper.selectById(storeId);
        if (store == null) {
            return false;
        }
        Integer status = store.getStatus();
        return status != null && status == StoreStatusEnum.OPEN.getCode();
    }

    /**
     * 校验顾客
     *
     * @param customerId 顾客ID
     */
    private boolean isCustomerAvailable(Long customerId) {
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null) {
            return false;
        }
        Integer status = customer.getStatus();
        return status != null && status == CustomerStatusEnum.ACTIVE.getCode();
    }

    /**
     * 批量加载并校验存在性
     *
     * @param ids ID集合
     * @param batchLoader 批量加载器
     * @param idExtractor ID提取器
     * @param errorCode 异常码
     * @param entityName 实体名称
     * @return ID到实体的映射
     */
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

    /**
     * 批量加载商品
     *
     * @param productIds 商品ID集合
     * @return 商品ID到商品的映射
     */
    private Map<Long, Product> loadProducts(Set<Long> productIds) {
        return loadByIds(productIds, productMapper::selectByIds, Product::getId,
                OrderErrorCode.PRODUCT_NOT_FOUND, "商品");
    }

    /**
     * 加载商品门店库存状态
     *
     * @param productIds 商品ID集合
     * @param storeId 门店ID
     * @return 商品ID到门店库存状态的映射
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
     * 加载商品封面图
     *
     * @param productIds 商品ID集合
     * @return 商品ID到封面图ID的映射
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
     * 批量加载商品封面图 URL
     * <p>基于商品ID到封面图ID的映射，关联 {@code gallery} 表解析出图片访问地址。</p>
     *
     * @param productCoverMap 商品ID到封面图ID的映射
     * @return 商品ID到封面图 URL 的映射（无封面或找不到图片的不包含）
     */
    private Map<Long, String> loadCoverUrls(Map<Long, Long> productCoverMap) {
        Map<Long, String> map = new HashMap<>();
        if (productCoverMap == null || productCoverMap.isEmpty()) {
            return map;
        }
        Set<Long> imageIds = new LinkedHashSet<>(productCoverMap.values());
        if (imageIds.isEmpty()) {
            return map;
        }
        Map<Long, String> urlMap = galleryMapper.selectByIds(new ArrayList<>(imageIds)).stream()
                .collect(Collectors.toMap(Gallery::getId, Gallery::getUrl, (a, b) -> a));
        productCoverMap.forEach((productId, imageId) -> {
            String url = urlMap.get(imageId);
            if (url != null) {
                map.put(productId, url);
            }
        });
        return map;
    }

    /**
     * 批量加载客制化项目
     *
     * @param itemIds 客制化项目ID集合
     * @return 客制化项目ID到客制化项目的映射
     */
    private Map<Long, Customization> loadCustomizations(Set<Long> itemIds) {
        return loadByIds(itemIds, customizationMapper::selectByIds, Customization::getId,
                OrderErrorCode.CUSTOMIZATION_NOT_FOUND, "客制化项目");
    }

    /**
     * 批量加载客制化选项
     *
     * @param optionIds 客制化选项ID集合
     * @return 选项ID到客制化选项的映射
     */
    private Map<Long, CustomizationOption> loadOptions(Set<Long> optionIds) {
        return loadByIds(optionIds, customizationOptionMapper::selectByIds, CustomizationOption::getId,
                OrderErrorCode.CUSTOMIZATION_OPTION_NOT_FOUND, "客制化选项");
    }

    /**
     * 加载客制化选项门店状态
     *
     * @param optionIds 选项ID集合
     * @param storeId 门店ID
     * @return 选项ID到门店状态的映射
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
     * 判断商品是否不可用
     *
     * @param product 商品
     * @param storeStatus 门店库存状态
     * @return 是否不可用
     */
    private boolean isProductUnavailable(Product product, Integer storeStatus) {
        boolean globalOffShelf = product.getStatus() == null
                || product.getStatus() != ProductGlobalStatusEnum.ON_SHELF.getCode();
        boolean storeSoldOut = storeStatus == null
                || storeStatus != ProductStoreStatusEnum.AVAILABLE.getCode();
        return globalOffShelf || storeSoldOut;
    }

    /**
     * 判断客制化选项是否不可用
     *
     * @param option 客制化选项
     * @param storeStatus 门店状态
     * @return 是否不可用
     */
    private boolean isOptionUnavailable(CustomizationOption option, Integer storeStatus) {
        boolean globalDisabled = option.getStatus() == null
                || option.getStatus() != CustomizationOptionGlobalStatusEnum.ACTIVE.getCode();
        boolean storeDisabled = storeStatus == null
                || storeStatus != CustomizationOptionGlobalStatusEnum.ACTIVE.getCode();
        return globalDisabled || storeDisabled;
    }

    /**
     * 判断客制化项目是否不可用
     *
     * @param customization 客制化项目
     * @return 是否不可用
     */
    private boolean isCustomizationUnavailable(Customization customization) {
        return customization == null
                || customization.getStatus() == null
                || customization.getStatus() != CustomizationStatusEnum.ACTIVE.getCode();
    }

    /**
     * 空金额转零
     *
     * @param value 金额
     * @return 金额（空则为零）
     */
    private static BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * ID列表拼接
     *
     * @param ids ID列表
     * @return 拼接后的字符串
     */
    private static String join(List<Long> ids) {
        return ids.stream().map(String::valueOf).collect(Collectors.joining("、"));
    }

    /**
     * skuId 解析结果：按商品项索引的逐项 ID 列表，以及去重后的全量 ID 集合。
     */
    private record SkuResolution(
            List<Long> productIds,
            List<List<Long>> optionIds,
            List<List<Long>> itemIds,
            Set<Long> allProductIds,
            Set<Long> allOptionIds,
            Set<Long> allItemIds) {}

    /**
     * 预构建阶段批量加载的关联实体上下文，供逐项分类与计价复用，避免重复查表。
     */
    private record LoadedEntities(
            Map<Long, Product> productMap,
            Map<Long, Long> productCoverMap,
            Map<Long, String> productCoverUrlMap,
            Map<Long, Integer> productStoreStatusMap,
            Map<Long, Customization> customizationMap,
            Map<Long, CustomizationOption> optionMap,
            Map<Long, Integer> optionStoreStatusMap) {}

    /**
     * 单个有效商品的计价结果：明细对象、数量与小计。
     */
    private record PricedItem(CreateOrderProductItem detail, int quantity, BigDecimal subtotal) {}
}
