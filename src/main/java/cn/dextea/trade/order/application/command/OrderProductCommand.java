package cn.dextea.trade.order.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 下单商品项命令（应用层）。
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderProductCommand {

    private String skuId;

    private Integer quantity;
}
