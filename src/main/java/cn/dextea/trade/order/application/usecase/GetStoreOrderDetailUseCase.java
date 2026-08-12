package cn.dextea.trade.order.application.usecase;

import cn.dextea.trade.order.application.assembler.StoreOrderDetailAssembler;
import cn.dextea.trade.order.application.dto.command.GetStoreOrderDetailCommand;
import cn.dextea.trade.order.application.dto.result.StoreOrderDetailResult;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.shared.util.EnsureUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetStoreOrderDetailUseCase {

    private final OrderRepository orderRepository;

    public StoreOrderDetailResult execute(GetStoreOrderDetailCommand command) {
        log.info("查询门店订单详情, storeId={}, orderId={}", command.getStoreId(), command.getOrderId());

        Order order = EnsureUtil.notNull(
                orderRepository.getOrderById(command.getOrderId()), OrderErrorCode.ORDER_NOT_FOUND);

        order.ensureBelongsToStore(command.getStoreId());

        StoreOrderDetailResult result = StoreOrderDetailAssembler.toResult(order);
        log.info("查询门店订单详情成功, storeId={}, orderId={}, itemCount={}",
                command.getStoreId(), command.getOrderId(),
                result.getItems() == null ? 0 : result.getItems().size());
        return result;
    }
}
