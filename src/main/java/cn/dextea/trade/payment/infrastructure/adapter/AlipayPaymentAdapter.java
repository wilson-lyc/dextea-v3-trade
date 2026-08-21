package cn.dextea.trade.payment.infrastructure.adapter;

import cn.dextea.trade.order.domain.dto.CreateTradeRequest;
import cn.dextea.trade.order.domain.dto.QueryTradeResult;
import cn.dextea.trade.order.domain.port.PaymentPort;
import cn.dextea.trade.payment.domain.exception.PayErrorCode;
import cn.dextea.trade.payment.infrastructure.config.AlipayProperties;
import cn.dextea.trade.shared.error.BizError;
import cn.dextea.trade.shared.model.Money;
import com.alipay.v3.ApiClient;
import com.alipay.v3.ApiException;
import com.alipay.v3.api.AlipayTradeApi;
import com.alipay.v3.model.AlipayTradeCreateDefaultResponse;
import com.alipay.v3.model.AlipayTradeCreateModel;
import com.alipay.v3.model.AlipayTradeCreateResponseModel;
import com.alipay.v3.model.AlipayTradeQueryDefaultResponse;
import com.alipay.v3.model.AlipayTradeQueryModel;
import com.alipay.v3.model.AlipayTradeQueryResponseModel;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlipayPaymentAdapter implements PaymentPort {

    private static final String PRODUCT_CODE = "JSAPI_PAY";

    /**
     * 支付宝时间字段格式，如 send_pay_date：yyyy-MM-dd HH:mm:ss
     */
    private static final DateTimeFormatter ALIPAY_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AlipayProperties alipayProperties;
    private final ApiClient alipayApiClient;
    private static final Tracer TRACER = GlobalOpenTelemetry.getTracer("alipay-adapter");

    @Override
    public String createTradeNo(CreateTradeRequest request) {
        AlipayTradeCreateModel model = new AlipayTradeCreateModel();
        model.setOutTradeNo(request.getOrderNo());
        model.setTotalAmount(resolveAmount(request));
        model.setSubject(alipayProperties.getSubject());
        model.setBuyerOpenId(request.getBuyerOpenId());
        model.setProductCode(PRODUCT_CODE);
        if (alipayProperties.getNotifyUrl() != null && !alipayProperties.getNotifyUrl().isBlank()) {
            model.setNotifyUrl(alipayProperties.getNotifyUrl());
        }
        if (request.getPayExpireAt() != null) {
            model.setTimeExpire(request.getPayExpireAt().format(ALIPAY_DATE_TIME_FORMAT));
        }

        AlipayTradeApi api = new AlipayTradeApi(alipayApiClient);
        Span span = TRACER.spanBuilder("alipay.trade.create").startSpan();
        try (Scope ignored = span.makeCurrent()) {
            String traceId = span.getSpanContext().getTraceId();
            if (traceId != null && !traceId.isEmpty()) {
                model.setPassbackParams(traceId);
            }
            span.setAttribute("alipay.out_trade_no", request.getOrderNo());
            log.info("调用支付宝创建交易, outTradeNo={}, subject={}, totalAmount={}, buyerOpenId={}, notifyUrl={}",
                    request.getOrderNo(), alipayProperties.getSubject(), resolveAmount(request),
                    request.getBuyerOpenId(), alipayProperties.getNotifyUrl());
            try {
                AlipayTradeCreateResponseModel response = api.create(model, null);
                if (response == null || response.getTradeNo() == null) {
                    throw new BizError(PayErrorCode.ALIPAY_CREATE_TRADE_FAILED, "支付宝未返回交易号");
                }
                span.setAttribute("alipay.trade_no", response.getTradeNo());
                log.info("支付宝创建交易成功, outTradeNo={}, tradeNo={}", request.getOrderNo(), response.getTradeNo());
                return response.getTradeNo();
            } catch (ApiException e) {
                AlipayTradeCreateDefaultResponse errorObject =
                        (AlipayTradeCreateDefaultResponse) e.getErrorObject();
                span.recordException(e);
                span.setStatus(StatusCode.ERROR);
                log.error("支付宝创建交易失败, outTradeNo={}, error={}", request.getOrderNo(), errorObject, e);
                throw new BizError(PayErrorCode.ALIPAY_CREATE_TRADE_FAILED,
                        "支付宝创建交易失败: " + errorObject);
            }
        } finally {
            span.end();
        }
    }

    private String resolveAmount(CreateTradeRequest request) {
        if (alipayProperties.getForceAmount() != null && !alipayProperties.getForceAmount().isBlank()) {
            return alipayProperties.getForceAmount();
        }
        return request.getTotalPrice().toString();
    }

    @Override
    public QueryTradeResult queryTrade(String outTradeNo) {
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(outTradeNo);

        AlipayTradeApi api = new AlipayTradeApi(alipayApiClient);
        Span span = TRACER.spanBuilder("alipay.trade.query").startSpan();
        try (Scope ignored = span.makeCurrent()) {
            span.setAttribute("alipay.out_trade_no", outTradeNo);
            log.info("调用支付宝交易查询, outTradeNo={}", outTradeNo);
            try {
                AlipayTradeQueryResponseModel response = api.query(model, null);
                if (response == null) {
                    throw new BizError(PayErrorCode.ALIPAY_QUERY_TRADE_FAILED, "支付宝未返回查询结果");
                }
                if (response.getTradeNo() != null) {
                    span.setAttribute("alipay.trade_no", response.getTradeNo());
                }
                log.info("支付宝交易查询成功, outTradeNo={}, tradeNo={}, tradeStatus={}",
                        outTradeNo, response.getTradeNo(), response.getTradeStatus());
                return QueryTradeResult.builder()
                        .outTradeNo(response.getOutTradeNo())
                        .tradeNo(response.getTradeNo())
                        .tradeStatus(response.getTradeStatus())
                        .totalAmount(response.getTotalAmount() == null
                                ? null : Money.of(new java.math.BigDecimal(response.getTotalAmount())))
                        .buyerUserId(response.getBuyerUserId())
                        .buyerOpenId(response.getBuyerOpenId())
                        .paidAt(parseAlipayDateTime(response.getSendPayDate()))
                        .build();
            } catch (ApiException e) {
                AlipayTradeQueryDefaultResponse errorObject =
                        (AlipayTradeQueryDefaultResponse) e.getErrorObject();
                span.recordException(e);
                span.setStatus(StatusCode.ERROR);
                log.error("支付宝交易查询失败, outTradeNo={}, error={}", outTradeNo, errorObject, e);
                throw new BizError(PayErrorCode.ALIPAY_QUERY_TRADE_FAILED,
                        "支付宝交易查询失败: " + errorObject);
            }
        } finally {
            span.end();
        }
    }

    private LocalDateTime parseAlipayDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, ALIPAY_DATE_TIME_FORMAT);
        } catch (Exception e) {
            log.warn("支付宝时间字段解析失败, value={}", value);
            return null;
        }
    }

}
