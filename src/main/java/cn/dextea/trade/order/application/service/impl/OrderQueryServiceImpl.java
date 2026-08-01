package cn.dextea.trade.order.application.service.impl;

import cn.dextea.trade.order.application.dto.OrderDetailDTO;
import cn.dextea.trade.order.application.dto.OrderItemDTO;
import cn.dextea.trade.order.application.dto.OrderStatusDTO;
import cn.dextea.trade.order.application.dto.OrderSummaryDTO;
import cn.dextea.trade.order.application.service.OrderQueryService;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.aggregate.Order;
import cn.dextea.trade.order.domain.model.aggregate.Store;
import cn.dextea.trade.order.domain.model.entity.OrderItem;
import cn.dextea.trade.order.domain.model.valueobject.CustomizationOption;
import cn.dextea.trade.order.domain.model.valueobject.PaymentStatus;
import cn.dextea.trade.order.domain.port.CustomizationRepository;
import cn.dextea.trade.order.domain.port.ProductRepository;
import cn.dextea.trade.order.domain.port.StoreRepository;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.order.domain.util.SkuIdParser;
import cn.dextea.trade.shared.domain.error.BizError;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomizationRepository customizationRepository;
    private final StoreRepository storeRepository;

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
                : productRepository.findCoverUrls(new ArrayList<>(allCoverIds));

        List<OrderSummaryDTO> result = new ArrayList<>(orders.size());
        for (Order order : orders) {
            List<String> coverUrls = coverIdsByOrder.getOrDefault(order.getId(), List.of()).stream()
                    .filter(Objects::nonNull)
                    .map(coverUrlMap::get)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            Store store = storeRepository.findStore(order.getStoreId());
            result.add(OrderSummaryDTO.builder()
                    .orderId(order.getId())
                    .storeName(store != null ? store.getName() : null)
                    .orderTime(order.getCreatedAt())
                    .tradeStatus(order.getPaymentStatus().getCode())
                    .makingStatus(order.getMakingStatus().getCode())
                    .totalPrice(order.getTotalPrice() == null ? null : order.getTotalPrice().getValue())
                    .totalQuantity(order.getTotalQuantity() == null ? null : order.getTotalQuantity().getValue())
                    .coverUrls(coverUrls)
                    .build());
        }
        return result;
    }

    @Override
    public OrderDetailDTO getOrderDetail(Long orderId, Long customerId) {
        Order order = loadOwnedOrder(orderId, customerId);

        Long storeId = null;
        String storeName = null;
        Store store = storeRepository.findStore(order.getStoreId());
        if (store != null) {
            storeId = store.getId();
            storeName = store.getName();
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
                        .customizationText(item.getCustomization() != null
                                ? item.getCustomization() : customizationTextMap.get(item.getId()))
                        .quantity(item.getQuantity() == null ? null : item.getQuantity().getValue())
                        .unitPrice(item.getUnitPrice() == null ? null : item.getUnitPrice().getValue())
                        .subtotal(item.getSubtotal() == null ? null : item.getSubtotal().getValue())
                        .build())
                .collect(Collectors.<OrderItemDTO>toList());

        return OrderDetailDTO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo() == null ? null : order.getOrderNo().getValue())
                .tradeNo(order.getTradeNo())
                .tradeStatus(order.getPaymentStatus().getCode())
                .makingStatus(order.getMakingStatus().getCode())
                .pickupCode(order.getPickupCode() == null ? null : order.getPickupCode().getValue())
                .totalPrice(order.getTotalPrice() == null ? null : order.getTotalPrice().getValue())
                .totalQuantity(order.getTotalQuantity() == null ? null : order.getTotalQuantity().getValue())
                .payMethod(order.getPaymentMethod() == null ? null : order.getPaymentMethod().getCode())
                .diningMethod(order.getDiningMethod() == null ? null : order.getDiningMethod().getCode())
                .note(order.getNote())
                .payExpireAt(order.getPaymentExpiredAt())
                .createdAt(order.getCreatedAt())
                .paidAt(order.getPaymentPaidAt())
                .refundedAt(order.getPaymentRefundedAt())
                .updatedAt(order.getUpdatedAt())
                .storeId(storeId)
                .storeName(storeName)
                .items(detailItems)
                .build();
    }

    @Override
    public OrderStatusDTO getOrderStatus(Long orderId, Long customerId) {
        Order order = loadOwnedOrder(orderId, customerId);
        boolean terminal = !Objects.equals(order.getPaymentStatus(), PaymentStatus.PENDING);
        return OrderStatusDTO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo() == null ? null : order.getOrderNo().getValue())
                .tradeNo(order.getTradeNo())
                .tradeStatus(order.getPaymentStatus().getCode())
                .makingStatus(order.getMakingStatus().getCode())
                .pickupCode(order.getPickupCode() == null ? null : order.getPickupCode().getValue())
                .payExpireAt(order.getPaymentExpiredAt())
                .paidAt(order.getPaymentPaidAt())
                .updatedAt(order.getUpdatedAt())
                .terminal(terminal)
                .build();
    }

    private Order loadOwnedOrder(Long orderId, Long customerId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new BizError(OrderErrorCode.ORDER_NOT_FOUND, "订单不存在: " + orderId);
        }
        if (!Objects.equals(order.getCustomerId(), customerId)) {
            throw new BizError(OrderErrorCode.ORDER_ACCESS_DENIED, "订单不属于该顾客: " + orderId);
        }
        return order;
    }

    private Map<Long, String> resolveCoverUrls(List<OrderItem> items) {
        Set<Long> coverIds = items.stream()
                .map(OrderItem::getCoverId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (coverIds.isEmpty()) {
            return Map.of();
        }
        return productRepository.findCoverUrls(new ArrayList<>(coverIds));
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
            for (CustomizationOption opt : customizationRepository.findOptions(new ArrayList<>(optionIds))) {
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
}
