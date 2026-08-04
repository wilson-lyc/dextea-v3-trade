package cn.dextea.trade.pay.infrastructure.adapter;

import cn.dextea.trade.order.domain.dto.CreateTradeRequest;
import cn.dextea.trade.order.domain.port.PaymentPort;
import cn.dextea.trade.pay.domain.exception.PayErrorCode;
import cn.dextea.trade.pay.infrastructure.config.AlipayProperties;
import cn.dextea.trade.shared.domain.error.BizError;
import com.alipay.v3.ApiClient;
import com.alipay.v3.ApiException;
import com.alipay.v3.api.AlipayTradeApi;
import com.alipay.v3.model.AlipayTradeCreateDefaultResponse;
import com.alipay.v3.model.AlipayTradeCreateModel;
import com.alipay.v3.model.AlipayTradeCreateResponseModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlipayPaymentAdapter implements PaymentPort {

    private static final String PRODUCT_CODE = "JSAPI_PAY";

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

}
