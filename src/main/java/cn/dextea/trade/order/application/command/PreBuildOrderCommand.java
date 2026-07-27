package cn.dextea.trade.order.application.command;

import cn.dextea.trade.pay.domain.enums.PlatformEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 预构建订单命令（应用层）。
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreBuildOrderCommand {

    private Long storeId;

    private Long customerId;

    private PlatformEnum platform;

    private Integer diningMethod;

    private String note;

    private List<OrderProductCommand> products;
}
