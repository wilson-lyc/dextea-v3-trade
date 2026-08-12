package cn.dextea.trade.order.application.usecase;

import cn.dextea.trade.order.application.assembler.StoreWindowOrderAssembler;
import cn.dextea.trade.order.application.dto.command.GetStoreWindowOrdersCommand;
import cn.dextea.trade.order.application.dto.result.GetStoreWindowOrdersResult;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetStoreWindowOrdersUseCase {

    private final OrderRepository orderRepository;
    private final StoreWindowOrderAssembler storeWindowOrderAssembler;

    public GetStoreWindowOrdersResult execute(GetStoreWindowOrdersCommand command) {
        LocalDateTime endAt = LocalDateTime.now();
        LocalDateTime startAt = endAt.minusHours(command.getHours());
        List<Order> orders = orderRepository.getStoreWindowOrders(command.getStoreId(), startAt, endAt);
        return storeWindowOrderAssembler.toResult(orders);
    }
}
