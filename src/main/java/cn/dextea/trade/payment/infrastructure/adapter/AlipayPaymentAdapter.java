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
        log.info("调用支付宝创建交易, outTradeNo={}, subject={}, totalAmount={}, buyerOpenId={}, notifyUrl={}",
                request.getOrderNo(), alipayProperties.getSubject(), resolveAmount(request),
                request.getBuyerOpenId(), alipayProperties.getNotifyUrl());
        try {
            AlipayTradeCreateResponseModel response = api.create(model, null);
            if (response == null || response.getTradeNo() == null) {
                throw new BizError(PayErrorCode.ALIPAY_CREATE_TRADE_FAILED, "支付宝未返回交易号");
            }
            log.info("支付宝创建交易成功, outTradeNo={}, tradeNo={}", request.getOrderNo(), response.getTradeNo());
            return response.getTradeNo();
        } catch (ApiException e) {
            AlipayTradeCreateDefaultResponse errorObject =
                    (AlipayTradeCreateDefaultResponse) e.getErrorObject();
            log.error("支付宝创建交易失败, outTradeNo={}, error={}", request.getOrderNo(), errorObject, e);
            throw new BizError(PayErrorCode.ALIPAY_CREATE_TRADE_FAILED,
                    "支付宝创建交易失败: " + errorObject);
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
        log.info("调用支付宝交易查询, outTradeNo={}", outTradeNo);
        try {
            AlipayTradeQueryResponseModel response = api.query(model, null);
            if (response == null) {
                throw new BizError(PayErrorCode.ALIPAY_QUERY_TRADE_FAILED, "支付宝未返回查询结果");
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
            log.error("支付宝交易查询失败, outTradeNo={}, error={}", outTradeNo, errorObject, e);
            throw new BizError(PayErrorCode.ALIPAY_QUERY_TRADE_FAILED,
                    "支付宝交易查询失败: " + errorObject);
        }
    }

    /**
     * 解析支付宝返回的时间字符串，解析失败不影响主流程，仅记录日志后返回 null。
     */
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
