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

    /**
     * 交易支付成功（商家可发货/服务）
     */
    private static final String TRADE_STATUS_SUCCESS = "TRADE_SUCCESS";

    /**
     * 交易结束，不可退款（同样属于已收款）
     */
    private static final String TRADE_STATUS_FINISHED = "TRADE_FINISHED";

    private String outTradeNo;

    private String tradeNo;

    private String tradeStatus;

    private Money totalAmount;

    private String buyerUserId;

    private String buyerOpenId;

    /**
     * 买家实际付款时间（支付宝 send_pay_date），未支付时为空
     */
    private LocalDateTime paidAt;

    /**
     * 支付渠道侧是否已收款成功。
     */
    public boolean isPaidStatus() {
        return TRADE_STATUS_SUCCESS.equals(tradeStatus) || TRADE_STATUS_FINISHED.equals(tradeStatus);
    }
}
