package cn.dextea.trade.pay.domain.model;
import lombok.Builder;
import lombok.Value;
@Value
@Builder
public class PaymentResult {
    String orderNo;
    String tradeNo;
    String platform;
    String rawStatus;
    boolean success;
    boolean closed;
    String traceId;
    public static PaymentResult evaluate(String orderNo, String tradeNo, String platform,
                                         String rawStatus, String traceId) {
        boolean success = "TRADE_SUCCESS".equals(rawStatus) || "TRADE_FINISHED".equals(rawStatus);
        boolean closed = "TRADE_CLOSED".equals(rawStatus);
        return PaymentResult.builder()
                .orderNo(orderNo)
                .tradeNo(tradeNo)
                .platform(platform)
                .rawStatus(rawStatus)
                .success(success)
                .closed(closed)
                .traceId(traceId)
                .build();
    }
}
