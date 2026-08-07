package cn.dextea.trade.order.application.usecase;

import cn.dextea.trade.order.application.assembler.OrderDetailAssembler;
import cn.dextea.trade.order.application.dto.command.GetOrderDetailCommand;
import cn.dextea.trade.order.application.dto.result.OrderDetailResult;
import cn.dextea.trade.order.application.service.PaymentReconciliationService;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.model.Store;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.order.domain.repository.StoreRepository;
import cn.dextea.trade.shared.error.BizError;
import cn.dextea.trade.shared.util.EnsureUtil;
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
    private final PaymentReconciliationService paymentReconciliationService;

    public OrderDetailResult execute(GetOrderDetailCommand command) {
        log.info("查询订单详情, customerId={}, orderId={}", command.getCustomerId(), command.getOrderId());

        Order order = EnsureUtil.notNull(orderRepository.getOrderById(command.getOrderId()), OrderErrorCode.ORDER_NOT_FOUND);

        order.ensureBelongsTo(command.getCustomerId());

        // 待支付时主动向支付渠道对账，有差异以渠道为准回写本地，再返回最新详情
        paymentReconciliationService.reconcileIfPending(order);

        OrderDetailResult result = OrderDetailAssembler.toResult(order, resolveStoreName(order.getStoreId()));
        log.info("查询订单详情成功, customerId={}, orderId={}, itemCount={}, paymentStatus={}",
                command.getCustomerId(), command.getOrderId(),
                result.getItems() == null ? 0 : result.getItems().size(), order.getPaymentStatus());
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
