package cn.dextea.trade.model;

import cn.dextea.trade.model.PaymentNotifyData;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * 支付平台回单消息体（MQ 消息 body 反序列化目标）。
 *
 * <p>消息由支付平台推送到 RocketMQ，结构示例如下：
 * <pre>
 * {
 *   "id": "97cd7118-...",
 *   "channel": "alipay",
 *   "trace_id": "015a2fe3-...",
 *   "timestamp": 1784962369823,
 *   "raw_body": "gmt_create=...&out_trade_no=...&trade_status=TRADE_SUCCESS&...",
 *   "headers": { "Content-Type": "application/x-www-form-urlencoded; charset=utf-8" },
 *   "data": { "out_trade_no": "...", "trade_no": "...", "trade_status": "TRADE_SUCCESS", "total_amount": "0.01", ... }
 * }
 * </pre>
 * 其中 {@link #data} 为支付平台解析后的业务字段，{@code out_trade_no} 对应本系统订单号。
 * </p>
 */
@Data
public class PaymentNotifyMessage {

    private String id;

    /** 支付渠道，如 alipay / weixin */
    private String channel;

    @JsonProperty("trace_id")
    private String traceId;

    private Long timestamp;

    @JsonProperty("raw_body")
    private String rawBody;

    private Map<String, String> headers;

    /** 支付平台解析后的业务数据 */
    private PaymentNotifyData data;
}
