package cn.dextea.trade.pay.domain.port;

/**
 * 支付结果同步端口
 */
public interface PaymentResultSyncPort {

    /**
     * 同步支付成功结果。
     *
     * @param orderNo   商户订单号
     * @param tradeNo   支付渠道交易号
     * @param settled   是否已结算（渠道状态为 TRADE_FINISHED）
     * @param rawStatus 渠道原始交易状态，仅用于日志
     * @param traceId   回单链路追踪 ID，仅用于日志
     */
    void syncPaid(String orderNo, String tradeNo, boolean settled, String rawStatus, String traceId);

    /**
     * 同步交易关闭结果（未付款超时关闭或支付后全额退款关闭）。
     *
     * @param orderNo  商户订单号（本系统订单号）
     * @param traceId  回单链路追踪 ID，仅用于日志
     */
    void syncClosed(String orderNo, String traceId);
}
