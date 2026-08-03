package cn.dextea.trade.order.application.usecase;

import cn.dextea.trade.order.application.dto.command.CreateOrderCommand;
import cn.dextea.trade.order.application.dto.result.OrderCreateResult;
import cn.dextea.trade.order.application.assembler.OrderItemAssembler;
import cn.dextea.trade.order.application.dto.shared.CreateOrderItem;
import cn.dextea.trade.order.domain.dto.CreateTradeRequest;
import cn.dextea.trade.order.domain.model.Customer;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.model.OrderItem;
import cn.dextea.trade.order.domain.model.Product;
import cn.dextea.trade.order.domain.model.Store;
import cn.dextea.trade.order.domain.port.OrderNoGenerator;
import cn.dextea.trade.order.domain.port.PaymentPort;
import cn.dextea.trade.order.domain.repository.CustomerRepository;
import cn.dextea.trade.order.domain.repository.ProductRepository;
import cn.dextea.trade.order.domain.repository.StoreRepository;
import cn.dextea.trade.order.domain.service.SkuIdService;
import cn.dextea.trade.shared.domain.enumeration.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreateOrderUseCase {

    private static final int PAY_EXPIRE_MINUTES = 15;

    private final StoreRepository storeRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final SkuIdService skuIdService;
    private final OrderNoGenerator orderNoGenerator;
    private final PaymentPort paymentPort;

    public OrderCreateResult execute(CreateOrderCommand command) {
        Store store = storeRepository.getStoreById(command.getStoreId());
        store.ensureActive();

        Customer customer = customerRepository.getCustomerById(command.getCustomerId());
        customer.ensureActive();

        List<String> skuIds = command.getItems().stream()
                .map(item -> item.getSkuId())
                .collect(Collectors.toList());
        Set<Long> productIds = skuIdService.extractProductIds(skuIds);
        Map<Long, Product> products = productRepository.getProductByIdsWithStoreId(productIds, store.getId());

        Order order = Order.create(command.getCustomerId(), command.getStoreId(), orderNoGenerator,
                command.getSource(), command.getPaymentMethod(), command.getDiningMethod(),
                command.getNote(), command.getIdempotencyKey());

        List<CreateOrderItem> availableItems = new ArrayList<>();
        List<CreateOrderItem> unavailableItems = new ArrayList<>();

        for (CreateOrderItem commandItem : command.getItems()) {
            Long productId = skuIdService.extractProductId(commandItem.getSkuId());
            Product product = products.get(productId);
            if (product == null) {
                throw new BizError(OrderErrorCode.PRODUCT_NOT_FOUND, "商品不存在: productId=" + productId);
            }
            
            OrderItem orderItem = order.addItem(product, commandItem.getSkuId(), commandItem.getQuantity());

            CreateOrderItem item = OrderItemAssembler.toCreateItem(orderItem);
            if (orderItem.getAvailable()) {
                availableItems.add(item);
            } else {
                unavailableItems.add(item);
            }
        }

        LocalDateTime paymentExpiredAt = LocalDateTime.now().plusMinutes(PAY_EXPIRE_MINUTES);
        String tradeNo = paymentPort.createTradeNo(CreateTradeRequest.builder()
                .orderNo(order.getOrderNo())
                .buyerOpenId(resolveBuyerOpenId(customer, command.getPaymentMethod()))
                .totalPrice(order.getTotalPrice())
                .totalQuantity(order.getTotalQuantity())
                .paymentMethod(command.getPaymentMethod())
                .payExpireAt(paymentExpiredAt)
                .build());
        order.markCreated(tradeNo, paymentExpiredAt);

        return OrderCreateResult.builder()
                .available(OrderItemAssembler.toPreBuildItems(availableItems))
                .unavailable(OrderItemAssembler.toPreBuildItems(unavailableItems))
                .totalQuantity(order.getTotalQuantity())
                .totalPrice(order.getTotalPrice())
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .tradeNo(order.getTradeNo())
                .paymentExpiredAt(order.getPaymentExpiredAt())
                .build();
    }

    private String resolveBuyerOpenId(Customer customer, PaymentMethod method) {
        if (method == PaymentMethod.WEIXIN) {
            return customer.getWeixinOpenId();
        }
        if (method == PaymentMethod.ALIPAY) {
            return customer.getAlipayOpenId();
        }
        return null;
    }
}
