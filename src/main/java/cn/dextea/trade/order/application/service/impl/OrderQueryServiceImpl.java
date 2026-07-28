package cn.dextea.trade.order.application.service.impl;

import cn.dextea.trade.common.error.BizError;
import cn.dextea.trade.order.application.dto.OrderDetailDTO;
import cn.dextea.trade.order.application.dto.OrderItemDTO;
import cn.dextea.trade.order.application.dto.OrderStatusDTO;
import cn.dextea.trade.order.application.dto.OrderSummaryDTO;
import cn.dextea.trade.order.application.dto.StoreInfoDTO;
import cn.dextea.trade.order.application.facade.ExternalDataFacade;
import cn.dextea.trade.order.application.service.OrderQueryService;
import cn.dextea.trade.order.domain.enums.DiningMethodEnum;
import cn.dextea.trade.order.domain.enums.MakingStatusEnum;
import cn.dextea.trade.order.domain.enums.TradeStatusEnum;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.aggregate.Order;
import cn.dextea.trade.order.domain.model.entity.OrderItem;
import cn.dextea.trade.order.domain.model.valueobject.CustomizationOption;
import cn.dextea.trade.order.domain.model.valueobject.Store;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.order.domain.util.SkuIdParser;
import cn.dextea.trade.pay.domain.enums.PlatformEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
 *
 * <p>封面 URL 由 {@link ExternalDataFacade}（底层为 ProductGateway）以 coverId → url
 * 的清洗结果提供，应用层不感知图库表结构。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderRepository orderRepository;
    private final ExternalDataFacade externalDataFacade;

    @Override
    public List<OrderSummaryDTO> getOrdersByCustomer(Long customerId, int year, int month) {
        LocalDate firstDay = LocalDate.of(year, month, 1);
        if (firstDay.isAfter(LocalDate.now())) {
            throw new BizError(OrderErrorCode.ORDER_QUERY_MONTH_INVALID, "查询年月不能晚于当前月份: " + year + "-" + month);
        }
        LocalDateTime start = firstDay.atStartOfDay();
        LocalDateTime end = firstDay.plusMonths(1).atStartOfDay();
        List<Order> orders = orderRepository.findByCustomerIdAndCreatedBetween(customerId, start, end);
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

        Map<Long, String> coverUrlMap = allCoverIds.isEmpty()
                ? Map.of()
                : externalDataFacade.findCoverUrls(new ArrayList<>(allCoverIds));

        List<OrderSummaryDTO> result = new ArrayList<>(orders.size());
        for (Order order : orders) {
            List<String> coverUrls = coverIdsByOrder.getOrDefault(order.getId(), List.of()).stream()
                    .filter(Objects::nonNull)
                    .map(coverUrlMap::get)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            Store store = externalDataFacade.findStore(order.getStoreId());
            result.add(OrderSummaryDTO.builder()
                    .storeName(store != null ? store.getName() : null)
                    .orderTime(order.getCreatedAt())
                    .tradeStatus(order.getTradeStatus())
                    .tradeStatusDesc(safeEnumDesc(() -> TradeStatusEnum.of(order.getTradeStatus()).getDescription()))
                    .makingStatus(order.getMakingStatus())
                    .makingStatusDesc(safeEnumDesc(() -> MakingStatusEnum.of(order.getMakingStatus()).getDescription()))
                    .totalPrice(order.getTotalPrice())
                    .totalQuantity(order.getTotalQuantity())
                    .payExpireAt(order.getPayExpireAt())
                    .coverUrls(coverUrls)
                    .build());
        }
        return result;
    }

    @Override
    public OrderDetailDTO getOrderDetail(Long orderId, Long customerId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new BizError(OrderErrorCode.ORDER_NOT_FOUND, "订单不存在: " + orderId);
        }
        if (!Objects.equals(order.getCustomerId(), customerId)) {
            throw new BizError(OrderErrorCode.ORDER_ACCESS_DENIED, "订单不属于该顾客: " + orderId);
        }

        StoreInfoDTO storeInfo = null;
        Store store = externalDataFacade.findStore(order.getStoreId());
        if (store != null) {
            storeInfo = StoreInfoDTO.builder()
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

        List<OrderItemDTO> detailItems = items.stream()
                .map(item -> OrderItemDTO.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .skuId(item.getSkuId())
                        .coverUrl(item.getCoverId() != null ? coverUrlMap.get(item.getCoverId()) : null)
                        .customizationText(customizationTextMap.get(item.getId()))
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.<OrderItemDTO>toList());

        return OrderDetailDTO.builder()
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
                .payExpireAt(order.getPayExpireAt())
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
        return externalDataFacade.findCoverUrls(new ArrayList<>(coverIds));
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
            for (CustomizationOption opt : externalDataFacade.findOptions(new ArrayList<>(optionIds))) {
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

    @Override
    public OrderStatusDTO getOrderStatus(Long orderId, Long customerId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new BizError(OrderErrorCode.ORDER_NOT_FOUND, "订单不存在: " + orderId);
        }
        if (!Objects.equals(order.getCustomerId(), customerId)) {
            throw new BizError(OrderErrorCode.ORDER_ACCESS_DENIED, "订单不属于该顾客: " + orderId);
        }

        Integer tradeStatus = order.getTradeStatus();
        boolean terminal = !Objects.equals(tradeStatus, TradeStatusEnum.TRADE_WAIT_PAY.getCode());
        return OrderStatusDTO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .tradeNo(order.getTradeNo())
                .tradeStatus(tradeStatus)
                .makingStatus(order.getMakingStatus())
                .payExpireAt(order.getPayExpireAt())
                .paidAt(order.getPaidAt())
                .updatedAt(order.getUpdatedAt())
                .terminal(terminal)
                .build();
    }

    private static String safeEnumDesc(Supplier<String> supplier) {
        try {
            return supplier.get();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
