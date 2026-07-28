package cn.dextea.trade.pay.api.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;
@Data
public class PaymentCallbackMessage {
    private String id;
    @JsonProperty("channel")
    private String platform;
    @JsonProperty("trace_id")
    private String traceId;
    private Long timestamp;
    @JsonProperty("raw_body")
    private String rawBody;
    private Map<String, String> headers;
    private PaymentCallbackData data;
}
