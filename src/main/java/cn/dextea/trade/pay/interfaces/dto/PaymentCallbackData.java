package cn.dextea.trade.pay.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 支付平台回单业务数据。
 */
@Data
public class PaymentCallbackData {

    /** 通知类型，如 trade_status_sync */
    @JsonProperty("notify_type")
    private String notifyType;

    /** 通知时间 */
    @JsonProperty("notify_time")
    private String notifyTime;

    /** 交易状态，如 TRADE_SUCCESS / TRADE_FINISHED / TRADE_CLOSED */
    @JsonProperty("trade_status")
    private String tradeStatus;

    /** 商户订单号，对应本系统 orders.order_no */
    @JsonProperty("out_trade_no")
    private String outTradeNo;

    /** 支付平台交易号，对应本系统 orders.trade_no */
    @JsonProperty("trade_no")
    private String tradeNo;

    /** 订单总金额（元） */
    @JsonProperty("total_amount")
    private String totalAmount;

    /** 实收金额（元） */
    @JsonProperty("receipt_amount")
    private String receiptAmount;

    /** 买家付款金额（元） */
    @JsonProperty("buyer_pay_amount")
    private String buyerPayAmount;

    /** 交易创建时间 */
    @JsonProperty("gmt_create")
    private String gmtCreate;

    /** 交易付款时间 */
    @JsonProperty("gmt_payment")
    private String gmtPayment;

    /** 卖家支付平台用户号 */
    @JsonProperty("seller_id")
    private String sellerId;

    /** 卖家支付平台账号 */
    @JsonProperty("seller_email")
    private String sellerEmail;

    /** 买家支付平台 openId */
    @JsonProperty("buyer_open_id")
    private String buyerOpenId;

    /** 买家支付平台账号（脱敏） */
    @JsonProperty("buyer_logon_id")
    private String buyerLogonId;

    /** 应用 AppId */
    @JsonProperty("app_id")
    private String appId;
}
