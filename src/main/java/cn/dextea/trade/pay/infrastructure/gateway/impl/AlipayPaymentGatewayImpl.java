package cn.dextea.trade.pay.infrastructure.gateway.impl;
import cn.dextea.trade.shared.domain.error.BizError;
import cn.dextea.trade.pay.domain.exception.PayErrorCode;
import cn.dextea.trade.pay.domain.gateway.PaymentGateway;
import cn.dextea.trade.pay.domain.model.aggregate.Payment;
import cn.dextea.trade.pay.infrastructure.config.AlipayPaymentConfig;
import com.alipay.v3.ApiClient;
import com.alipay.v3.ApiException;
import com.alipay.v3.api.AlipayTradeApi;
import com.alipay.v3.model.AlipayTradeCreateModel;
import com.alipay.v3.model.AlipayTradeCreateResponseModel;
import com.alipay.v3.Configuration;
import com.alipay.v3.util.model.AlipayConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
@Slf4j
@Component
public class AlipayPaymentGatewayImpl implements PaymentGateway {
    private static final DateTimeFormatter TIME_EXPIRE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final AlipayTradeApi tradeApi;
    private final AlipayPaymentConfig config;
    public AlipayPaymentGatewayImpl(AlipayPaymentConfig config) {
        this.config = config;
        this.tradeApi = new AlipayTradeApi(buildApiClient(config));
    }
    private static ApiClient buildApiClient(AlipayPaymentConfig config) {
        ApiClient client = Configuration.getDefaultApiClient();
        AlipayConfig sdkConfig = new AlipayConfig();
        sdkConfig.setServerUrl(config.getServerUrl());
        sdkConfig.setAppId(config.getAppId());
        sdkConfig.setPrivateKey(config.getPrivateKey());
        sdkConfig.setAlipayPublicKey(config.getAlipayPublicKey());
        try {
            client.setAlipayConfig(sdkConfig);
        } catch (ApiException e) {
            throw new IllegalStateException("初始化支付宝 SDK 配置失败", e);
        }
        return client;
    }
    @Override
    public String createPayment(Payment payment) {
        AlipayTradeCreateModel model = toAlipayTradeCreateModel(payment);
        BigDecimal forceAmount = config.getForceAmount();
        if (forceAmount != null) {
            log.warn("开发环境：将订单 {} 的交易金额从 {} 覆盖为固定值 {}",
                    payment.getOrderNo(), payment.getTotalPrice(), forceAmount);
            model.totalAmount(forceAmount.toPlainString());
        }
        String notifyUrl = config.getNotifyUrl();
        if (notifyUrl != null && !notifyUrl.isBlank()) {
            model.notifyUrl(notifyUrl);
        }
        try {
            AlipayTradeCreateResponseModel resp = tradeApi.create(model);
            if (resp == null || resp.getTradeNo() == null) {
                throw new BizError(PayErrorCode.ALIPAY_TRADE_CREATE_FAILED,
                        "支付宝创建交易返回为空 outTradeNo=" + payment.getOrderNo());
            }
            return resp.getTradeNo();
        } catch (ApiException e) {
            log.error("alipay.trade.create 调用失败 outTradeNo={}", payment.getOrderNo(), e);
            throw new BizError(PayErrorCode.ALIPAY_TRADE_CREATE_FAILED,
                    "支付宝创建交易失败: " + e.getMessage());
        }
    }
    private AlipayTradeCreateModel toAlipayTradeCreateModel(Payment payment) {
        AlipayTradeCreateModel model = new AlipayTradeCreateModel()
                .outTradeNo(payment.getOrderNo())
                .totalAmount(payment.getTotalPrice().toPlainString())
                .productCode("JSAPI_PAY")
                .subject(config.getSubject())
                .opAppId(config.getAppId())
                .buyerOpenId(payment.getCustomerOpenId());
        if (payment.getPayExpireAt() != null) {
            model.timeExpire(payment.getPayExpireAt().format(TIME_EXPIRE_FORMATTER));
        }
        return model;
    }
}
