package cn.dextea.trade.order.application.usecase;

import cn.dextea.trade.order.application.assembler.StoreMakingBoardAssembler;
import cn.dextea.trade.order.application.dto.command.GetStoreMakingBoardCommand;
import cn.dextea.trade.order.application.dto.result.GetStoreMakingBoardResult;
import cn.dextea.trade.order.domain.enumeration.MakingStatus;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetStoreMakingBoardUseCase {

    private static final List<MakingStatus> BOARD_MAKING_STATUSES = List.of(MakingStatus.PREPARING, MakingStatus.READY);

    private final OrderRepository orderRepository;
    private final StoreMakingBoardAssembler storeMakingBoardAssembler;

    @Transactional(readOnly = true)
    public GetStoreMakingBoardResult execute(GetStoreMakingBoardCommand command) {
        List<Order> orders = orderRepository.getStoreOrdersByMakingStatuses(
                command.getStoreId(), BOARD_MAKING_STATUSES);
        return storeMakingBoardAssembler.toResult(orders);
    }
}
