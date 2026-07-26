package cn.dextea.trade.order.application.impl;

import cn.dextea.trade.catalog.domain.model.CustomizationOption;
import cn.dextea.trade.catalog.domain.model.Gallery;
import cn.dextea.trade.catalog.domain.model.Store;
import cn.dextea.trade.common.error.BizError;
import cn.dextea.trade.order.application.OrderQueryService;
import cn.dextea.trade.order.domain.enums.DiningMethodEnum;
import cn.dextea.trade.order.domain.enums.MakingStatusEnum;
import cn.dextea.trade.order.domain.enums.TradeStatusEnum;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.model.OrderDetailItemView;
import cn.dextea.trade.order.domain.model.OrderDetailView;
import cn.dextea.trade.order.domain.model.OrderItem;
import cn.dextea.trade.order.domain.model.OrderSummaryView;
import cn.dextea.trade.order.domain.model.StoreInfoView;
import cn.dextea.trade.order.domain.port.OrderRepository;
import cn.dextea.trade.order.domain.port.ProductCatalogPort;
import cn.dextea.trade.order.domain.port.StorePort;
import cn.dextea.trade.order.domain.util.SkuIdParser;
import cn.dextea.trade.pay.domain.model.PlatformEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 订单查询应用服务实现：组装订单列表与详情视图。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderRepository orderRepository;
    private final ProductCatalogPort productCatalogPort;
    private final StorePort storePort;

    @Override
    public List<OrderSummaryView> getOrdersByCustomer(Long customerId) {
        LocalDateTime since = LocalDateTime.now().minusMonths(3);
        List<Order> orders = orderRepository.findByCustomerIdAndCreatedAfter(customerId, since);
        if (orders.isEmpty()) {
            return List.of();
        }

        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        Map<Long, List<Long>> coverIdsByOrder = new HashMap<>();
        Set<Long> allCoverIds = new LinkedHashSet<>();
        for (OrderItem item : orderRepository.findItemsByOrderIds(orderIds)) {
            Long coverId = item.getCoverId();
            coverIdsByOrder.computeIfAbsent(item.getOrderId(), k -> new ArrayList<>()).add(coverId);
            if (coverId != null) {
                allCoverIds.add(coverId);
            }
        }

        Map<Long, String> coverUrlMap = new HashMap<>();
        if (!allCoverIds.isEmpty()) {
            coverUrlMap.putAll(productCatalogPort.findGalleries(new ArrayList<>(allCoverIds)).stream()
                    .collect(Collectors.toMap(Gallery::getId, Gallery::getUrl, (a, b) -> a)));
        }

        List<OrderSummaryView> result = new ArrayList<>(orders.size());
        for (Order order : orders) {
            List<String> coverUrls = coverIdsByOrder.getOrDefault(order.getId(), List.of()).stream()
                    .filter(Objects::nonNull)
                    .map(coverUrlMap::get)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            Store store = storePort.findById(order.getStoreId());
            result.add(OrderSummaryView.builder()
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

    @Override
    public OrderDetailView getOrderDetail(Long orderId, Long customerId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new BizError(OrderErrorCode.ORDER_NOT_FOUND, "订单不存在: " + orderId);
        }
        if (!Objects.equals(order.getCustomerId(), customerId)) {
            throw new BizError(OrderErrorCode.ORDER_ACCESS_DENIED, "订单不属于该顾客: " + orderId);
        }

        StoreInfoView storeInfo = null;
        Store store = storePort.findById(order.getStoreId());
        if (store != null) {
            storeInfo = StoreInfoView.builder()
                    .id(store.getId())
                    .name(store.getName())
                    .address(store.getAddress())
                    .phone(store.getPhone())
                    .businessHours(store.getBusinessHours())
                    .build();
        }

        List<OrderItem> items = orderRepository.findFullItemsByOrderId(orderId);
        Map<Long, String> coverUrlMap = resolveCoverUrls(items);
        Map<Long, String> customizationTextMap = resolveCustomizationTexts(items);

        List<OrderDetailItemView> detailItems = items.stream()
                .map(item -> OrderDetailItemView.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .skuId(item.getSkuId())
                        .coverUrl(item.getCoverId() != null ? coverUrlMap.get(item.getCoverId()) : null)
                        .customizationText(customizationTextMap.get(item.getId()))
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.<OrderDetailItemView>toList());

        return OrderDetailView.builder()
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
                .payMethodDesc(safeEnumDesc(() -> PlatformEnum.of(order.getPayMethod()).getDescription()))
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

    private Map<Long, String> resolveCoverUrls(List<OrderItem> items) {
        Set<Long> coverIds = items.stream()
                .map(OrderItem::getCoverId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (coverIds.isEmpty()) {
            return Map.of();
        }
        return productCatalogPort.findGalleries(new ArrayList<>(coverIds)).stream()
                .collect(Collectors.toMap(Gallery::getId, Gallery::getUrl, (a, b) -> a));
    }

    private Map<Long, String> resolveCustomizationTexts(List<OrderItem> items) {
        Set<Long> optionIds = new LinkedHashSet<>();
        for (OrderItem item : items) {
            if (item.getSkuId() != null) {
                optionIds.addAll(SkuIdParser.parseOptionIds(item.getSkuId()));
            }
        }
        Map<Long, String> nameMap = new HashMap<>();
        if (!optionIds.isEmpty()) {
            for (CustomizationOption opt : productCatalogPort.findOptions(new ArrayList<>(optionIds))) {
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

    private static String safeEnumDesc(Supplier<String> supplier) {
        try {
            return supplier.get();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
