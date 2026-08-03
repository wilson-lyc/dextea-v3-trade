package cn.dextea.trade.pay.interface_.mq.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
@Data
public class PaymentCallbackData {
    @JsonProperty("notify_type")
    private String notifyType;
    @JsonProperty("notify_time")
    private String notifyTime;
    @JsonProperty("trade_status")
    private String tradeStatus;
    @JsonProperty("out_trade_no")
    private String outTradeNo;
    @JsonProperty("trade_no")
    private String tradeNo;
    @JsonProperty("total_amount")
    private String totalAmount;
    @JsonProperty("receipt_amount")
    private String receiptAmount;
    @JsonProperty("buyer_pay_amount")
    private String buyerPayAmount;
    @JsonProperty("gmt_create")
    private String gmtCreate;
    @JsonProperty("gmt_payment")
    private String gmtPayment;
    @JsonProperty("seller_id")
    private String sellerId;
    @JsonProperty("seller_email")
    private String sellerEmail;
    @JsonProperty("buyer_open_id")
    private String buyerOpenId;
    @JsonProperty("buyer_logon_id")
    private String buyerLogonId;
    @JsonProperty("app_id")
    private String appId;
}
