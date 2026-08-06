package cn.dextea.trade.order.interfaces.event;

import cn.dextea.trade.order.application.dto.command.MarkOrderPaidCommand;
import cn.dextea.trade.order.application.usecase.MarkOrderPaidUseCase;
import cn.dextea.trade.shared.event.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaidEventListener {

    private final MarkOrderPaidUseCase markOrderPaidUseCase;

    @EventListener
    public void onOrderPaid(OrderPaidEvent event) {
        log.info("收到订单已支付事件, orderNo={}, tradeNo={}, platform={}, paidAt={}",
                event.orderNo(), event.tradeNo(), event.platform(), event.paidAt());
        markOrderPaidUseCase.execute(MarkOrderPaidCommand.builder()
                .orderNo(event.orderNo())
                .tradeNo(event.tradeNo())
                .paidAt(event.paidAt())
                .paidAmount(event.amount())
                .build());
    }
}
