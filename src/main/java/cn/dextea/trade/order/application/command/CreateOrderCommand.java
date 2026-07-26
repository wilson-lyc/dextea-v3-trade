package cn.dextea.trade.order.application.command;

import cn.dextea.trade.pay.domain.model.PlatformEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建订单命令（应用层）。
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderCommand {

    private Long storeId;

    private Long customerId;

    private PlatformEnum platform;

    private Integer diningMethod;

    private String note;

    private List<OrderProductCommand> products;

    private String idempotencyKey;
}
