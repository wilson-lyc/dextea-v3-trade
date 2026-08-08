package cn.dextea.trade.order.domain.dto;

import cn.dextea.trade.shared.model.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryTradeResult {

    private static final String TRADE_STATUS_SUCCESS = "TRADE_SUCCESS";
    private static final String TRADE_STATUS_FINISHED = "TRADE_FINISHED";
    private static final String TRADE_STATUS_CLOSED = "TRADE_CLOSED";

    private String outTradeNo;

    private String tradeNo;

    private String tradeStatus;

    private Money totalAmount;

    private String buyerUserId;

    private String buyerOpenId;

    private LocalDateTime paidAt;

    public boolean isPaidStatus() {
        return TRADE_STATUS_SUCCESS.equals(tradeStatus) || TRADE_STATUS_FINISHED.equals(tradeStatus);
    }

    public boolean isClosedStatus() {
        return TRADE_STATUS_CLOSED.equals(tradeStatus);
    }
}
