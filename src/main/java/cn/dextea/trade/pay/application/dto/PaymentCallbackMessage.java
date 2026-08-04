package cn.dextea.trade.pay.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record PaymentCallbackMessage(
        String id,
        String platform,
        @JsonProperty("trace_id") String traceId,
        Long timestamp,
        @JsonProperty("raw_body") String rawBody,
        Map<String, String> headers,
        Map<String, String> data) {
}
