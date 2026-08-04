package cn.dextea.trade.order.domain.dto;

import cn.dextea.trade.shared.domain.model.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryTradeResult {

    private String outTradeNo;

    private String tradeNo;

    private String tradeStatus;

    private Money totalAmount;

    private String buyerUserId;

    private String buyerOpenId;
}
