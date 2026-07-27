package cn.dextea.trade.pay.infrastructure.gateway.impl;

import cn.dextea.trade.common.error.BizError;
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

/**
 * 支付宝支付网关实现：{@link PaymentGateway} 的支付宝实现，封装支付宝 SDK 细节。
 */
@Slf4j
@Component
public class AlipayPaymentGatewayImpl implements PaymentGateway {

    private final AlipayTradeApi tradeApi;
    private final AlipayPaymentConfig config;

    public AlipayPaymentGatewayImpl(AlipayPaymentConfig config) {
        this.config = config;
        this.tradeApi = new AlipayTradeApi(buildApiClient(config));
    }

    /**
     * 构建支付宝 API 客户端。
     *
     * <p>从全局默认的 {@link ApiClient} 获取实例，并将网关配置（网关地址、appId、密钥等）
     * 封装为 SDK 的 {@link AlipayConfig} 写入客户端，完成支付宝 SDK 的初始化。</p>
     *
     * @param config 支付宝支付配置（来自 {@link AlipayPaymentConfig}）
     * @return 已完成配置初始化的 {@link ApiClient} 实例
     */
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
        // 由支付领域对象转换为支付宝 SDK 所需的 AlipayTradeCreateModel
        AlipayTradeCreateModel model = toAlipayTradeCreateModel(payment);
        // 开发/测试环境下，将订单总额强制限制为固定金额，避免产生真实交易金额
        BigDecimal forceAmount = config.getForceAmount();
        if (forceAmount != null) {
            log.warn("开发环境：将订单 {} 的交易金额从 {} 覆盖为固定值 {}",
                    payment.getOrderNo(), payment.getTotalPrice(), forceAmount);
            model.totalAmount(forceAmount.toPlainString());
        }
        // 支付宝异步支付回调地址（notify_url）：非空才设置，空则不传给支付宝
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

    /**
     * 转换为支付宝 SDK 所需的 {@link AlipayTradeCreateModel}，仅填充必填字段。
     */
    private AlipayTradeCreateModel toAlipayTradeCreateModel(Payment payment) {
        return new AlipayTradeCreateModel()
                .outTradeNo(payment.getOrderNo())
                .totalAmount(payment.getTotalPrice().toPlainString())
                .productCode("JSAPI_PAY")
                .subject(config.getSubject())
                .opAppId(config.getAppId())
                .buyerOpenId(payment.getCustomerOpenId());
    }
}
