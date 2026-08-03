package cn.dextea.trade.order.application.usecase;

import cn.dextea.trade.order.application.assembler.OrderItemAssembler;
import cn.dextea.trade.order.application.dto.command.PreBuildOrderCommand;
import cn.dextea.trade.order.application.dto.result.PreBuildOrderResult;
import cn.dextea.trade.order.application.dto.shared.PreBuildOrderItem;
import cn.dextea.trade.order.domain.model.Customer;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.model.OrderItem;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.Product;
import cn.dextea.trade.order.domain.model.Store;
import cn.dextea.trade.shared.domain.error.BizError;
import cn.dextea.trade.order.domain.repository.CustomerRepository;
import cn.dextea.trade.order.domain.repository.ProductRepository;
import cn.dextea.trade.order.domain.repository.StoreRepository;
import cn.dextea.trade.order.domain.service.SkuIdService;
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

        Order order = Order.create(command.getCustomerId(), command.getStoreId());

        List<PreBuildOrderItem> availableItems = new ArrayList<>();
        List<PreBuildOrderItem> unavailableItems = new ArrayList<>();

        for (PreBuildOrderItem commandItem : command.getItems()) {
            Long productId = skuIdService.extractProductId(commandItem.getSkuId());
            Product product = products.get(productId);
            if (product == null) {
                throw new BizError(OrderErrorCode.PRODUCT_NOT_FOUND, "商品不存在: productId=" + productId);
            }
            
            OrderItem orderItem = order.addItem(product, commandItem.getSkuId(), commandItem.getQuantity());

            PreBuildOrderItem item = OrderItemAssembler.toPreBuildItem(orderItem);
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
}
