package cn.dextea.trade.order.application.usecase;

import cn.dextea.trade.order.application.assembler.OrderDetailAssembler;
import cn.dextea.trade.order.application.dto.command.GetOrderDetailCommand;
import cn.dextea.trade.order.application.dto.result.OrderDetailResult;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.model.Store;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.order.domain.repository.StoreRepository;
import cn.dextea.trade.shared.domain.error.BizError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetOrderDetailUseCase {

    private static final String UNKNOWN_STORE_NAME = "未知门店";

    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;

    public OrderDetailResult execute(GetOrderDetailCommand command) {
        log.info("查询订单详情, customerId={}, orderId={}", command.getCustomerId(), command.getOrderId());

        Order order = orderRepository.getOrderById(command.getOrderId());
        if (order == null) {
            throw new BizError(OrderErrorCode.ORDER_NOT_FOUND);
        }

        order.ensureBelongsTo(command.getCustomerId());

        OrderDetailResult result = OrderDetailAssembler.toResult(order, resolveStoreName(order.getStoreId()));
        log.info("查询订单详情成功, customerId={}, orderId={}, itemCount={}",
                command.getCustomerId(), command.getOrderId(), result.getItems() == null ? 0 : result.getItems().size());
        return result;
    }

    private String resolveStoreName(Long storeId) {
        if (storeId == null) {
            return UNKNOWN_STORE_NAME;
        }
        Store store = storeRepository.getStoresByIds(Collections.singleton(storeId)).get(storeId);
        return store == null || store.getName() == null ? UNKNOWN_STORE_NAME : store.getName();
    }
}
