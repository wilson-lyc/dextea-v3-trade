package cn.dextea.trade.order.application.usecase;

import cn.dextea.trade.order.application.dto.command.PreBuildOrderCommand;
import cn.dextea.trade.order.application.dto.result.PreBuildOrderResult;
import cn.dextea.trade.order.application.dto.shared.PreBuildOrderItem;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.Customer;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.model.OrderItem;
import cn.dextea.trade.order.domain.model.Product;
import cn.dextea.trade.order.domain.model.Store;
import cn.dextea.trade.order.domain.repository.CustomerRepository;
import cn.dextea.trade.order.domain.repository.ProductRepository;
import cn.dextea.trade.order.domain.repository.StoreRepository;
import cn.dextea.trade.order.domain.service.SkuIdService;
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
        if (!store.isActive()) {
            throw new BizError(OrderErrorCode.STORE_INACTIVE);
        }

        Customer customer = customerRepository.getCustomerById(command.getCustomerId());
        if (!customer.isActive()) {
            throw new BizError(OrderErrorCode.CUSTOMER_INACTIVE);
        }

        List<String> skuIds = command.getItems().stream()
                .map(item -> item.getSkuId())
                .collect(Collectors.toList());
        Set<Long> productIds = skuIdService.extractProductIds(skuIds);
        Map<Long, Product> products = productRepository.getProductByIdsWithStoreId(productIds, store.getId());

        List<PreBuildOrderItem> availableItems = new ArrayList<>();
        List<PreBuildOrderItem> unavailableItems = new ArrayList<>();
        List<OrderItem> orderItems = new ArrayList<>();

        for (PreBuildOrderItem commandItem : command.getItems()) {
            Product product = products.get(commandItem.getProductId());
            OrderItem orderItem = skuIdService.buildOrderItem(
                    product, commandItem.getSkuId(), commandItem.getQuantity());
            orderItems.add(orderItem);

            PreBuildOrderItem item = toResultItem(orderItem);
            if (orderItem.getAvailable()) {
                availableItems.add(item);
            } else {
                unavailableItems.add(item);
            }
        }

        Order order = Order.prebuild(command.getCustomerId(), command.getStoreId(), orderItems);

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
                .customization(orderItem.getCustomization())
                .cover(orderItem.getCover())
                .unitPrice(orderItem.getUnitPrice())
                .totalPrice(orderItem.getTotalPrice())
                .available(orderItem.getAvailable())
                .unavailableReason(orderItem.getUnavailableReason())
                .build();
    }
}
