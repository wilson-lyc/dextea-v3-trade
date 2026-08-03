package cn.dextea.trade.order.application.usecase;

import cn.dextea.trade.order.application.dto.command.PreBuildOrderCommand;
import cn.dextea.trade.order.application.dto.result.PreBuildOrderResult;
import cn.dextea.trade.order.application.dto.shared.PreBuildOrderItem;
import cn.dextea.trade.order.domain.model.Customer;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.model.OrderItem;
import cn.dextea.trade.order.domain.model.Product;
import cn.dextea.trade.order.domain.model.Store;
import cn.dextea.trade.order.domain.repository.CustomerRepository;
import cn.dextea.trade.order.domain.repository.ProductRepository;
import cn.dextea.trade.order.domain.repository.StoreRepository;
import cn.dextea.trade.order.domain.service.SkuIdService;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.shared.domain.error.BizError;
import cn.dextea.trade.shared.domain.money.Money;
import cn.dextea.trade.shared.domain.quantity.Quantity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PreBuildOrderUseCase {

    private final StoreRepository storeRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final SkuIdService skuIdService;

    public PreBuildOrderResult execute(PreBuildOrderCommand command) {
        Store store = storeRepository.getStoreById(command.getStoreId());
        store.ensureActive();

        Customer customer = customerRepository.getCustomerById(command.getCustomerId());
        customer.ensureActive();

        List<String> skuIds = command.getItems().stream()
                .map(item -> item.getSkuId())
                .collect(Collectors.toList());
        Set<Long> productIds = skuIdService.extractProductIds(skuIds);
        Map<Long, Product> products = productRepository.getProductByIdsWithStoreId(productIds, store.getId());

        Order order = Order.prebuild(command.getCustomerId(), command.getStoreId());

        List<PreBuildOrderItem> availableItems = new ArrayList<>();
        List<PreBuildOrderItem> unavailableItems = new ArrayList<>();

        for (PreBuildOrderItem commandItem : command.getItems()) {
            Long productId = skuIdService.extractProductId(commandItem.getSkuId());
            Product product = products.get(productId);
            OrderItem orderItem = order.addItem(product, commandItem.getSkuId(), commandItem.getQuantity());

            PreBuildOrderItem item = toResultItem(orderItem);
            if (orderItem.getAvailable()) {
                availableItems.add(item);
            } else {
                unavailableItems.add(item);
            }
        }

        return PreBuildOrderResult.builder()
                .available(availableItems)
                .unavailable(unavailableItems)
                .totalQuantity(order.getTotalQuantity())
                .totalPrice(order.getTotalPrice())
                .build();
    }

    private PreBuildOrderItem toResultItem(OrderItem orderItem) {
        return PreBuildOrderItem.builder()
                .skuId(orderItem.getSkuId())
                .quantity(orderItem.getQuantity())
                .product(orderItem.getProductName())
                .customization(toOptionLabels(orderItem.getCustomization()))
                .cover(orderItem.getCover())
                .unitPrice(orderItem.getUnitPrice())
                .totalPrice(orderItem.getTotalPrice())
                .available(orderItem.getAvailable())
                .build();
    }

    private String toOptionLabels(String customization) {
        if (customization == null || customization.isEmpty()) {
            return customization;
        }
        return java.util.Arrays.stream(customization.split("-"))
                .map(segment -> {
                    String[] parts = segment.split("_");
                    return parts[parts.length - 1];
                })
                .collect(Collectors.joining(" / "));
    }
}
