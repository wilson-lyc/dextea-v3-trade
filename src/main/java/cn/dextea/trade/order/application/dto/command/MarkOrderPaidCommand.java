package cn.dextea.trade.order.application.dto.command;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@SuperBuilder
@NoArgsConstructor
public class MarkOrderPaidCommand {

    private String orderNo;

    private String tradeNo;

    private BigDecimal paidAmount;
}
