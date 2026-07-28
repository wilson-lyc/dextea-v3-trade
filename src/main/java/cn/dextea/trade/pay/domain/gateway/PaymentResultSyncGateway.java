package cn.dextea.trade.pay.domain.gateway;
public interface PaymentResultSyncGateway {
    void syncPaid(String orderNo, String tradeNo, String rawStatus, String traceId);
    void syncClosed(String orderNo, String traceId);
}
