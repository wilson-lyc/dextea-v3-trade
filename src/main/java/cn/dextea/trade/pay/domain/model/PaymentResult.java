package cn.dextea.trade.pay.domain.model;

import lombok.Builder;
import lombok.Value;

/**
 * 支付结果值对象：由支付渠道回单解析而来，渠道无关。
 */
@Value
@Builder
public class PaymentResult {

    /** 商户订单号，对应本系统订单号 */
    String orderNo;

    /** 支付渠道交易号（trade_no） */
    String tradeNo;

    /** 支付渠道，如 alipay / weixin */
    String channel;

    /** 渠道原始交易状态，如 TRADE_SUCCESS / TRADE_FINISHED / TRADE_CLOSED */
    String rawStatus;

    /** 是否支付成功（TRADE_SUCCESS / TRADE_FINISHED） */
    boolean success;

    /** 是否已结算（TRADE_FINISHED） */
    boolean settled;

    /** 是否交易关闭（TRADE_CLOSED） */
    boolean closed;

    /** 回单链路追踪 ID，用于日志排查 */
    String traceId;

    /**
     * 由渠道原始交易状态解析支付结果。
     */
    public static PaymentResult evaluate(String orderNo, String tradeNo, String channel,
                                         String rawStatus, String traceId) {
        boolean success = "TRADE_SUCCESS".equals(rawStatus) || "TRADE_FINISHED".equals(rawStatus);
        boolean settled = "TRADE_FINISHED".equals(rawStatus);
        boolean closed = "TRADE_CLOSED".equals(rawStatus);
        return PaymentResult.builder()
                .orderNo(orderNo)
                .tradeNo(tradeNo)
                .channel(channel)
                .rawStatus(rawStatus)
                .success(success)
                .settled(settled)
                .closed(closed)
                .traceId(traceId)
                .build();
    }
}
