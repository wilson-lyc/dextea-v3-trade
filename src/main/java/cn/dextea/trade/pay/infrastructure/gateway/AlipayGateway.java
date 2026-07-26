package cn.dextea.trade.pay.infrastructure.gateway;

import cn.dextea.trade.exception.BizError;
import cn.dextea.trade.pay.domain.exception.PayErrorCode;
import cn.dextea.trade.pay.domain.gateway.PaymentGateway;
import cn.dextea.trade.pay.domain.model.Payment;
import cn.dextea.trade.pay.infrastructure.config.AlipayGatewayConfig;
import com.alipay.v3.ApiClient;
import com.alipay.v3.ApiException;
import com.alipay.v3.api.AlipayTradeApi;
import com.alipay.v3.model.AlipayTradeCreateModel;
import com.alipay.v3.model.AlipayTradeCreateResponseModel;
import com.alipay.v3.util.model.AlipayConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 支付宝网关：{@link PaymentGateway} 的支付宝实现，封装支付宝 SDK 细节。
 * <p>网关自身负责依据 {@link AlipayGatewayConfig} 构建支付宝 SDK 的 {@link ApiClient}
 * 与 {@link AlipayTradeApi}，无需外部额外提供客户端 Bean。</p>
 */
@Slf4j
@Component
public class AlipayGateway implements PaymentGateway {

    private final AlipayTradeApi tradeApi;
    private final AlipayGatewayConfig alipayConfig;

    public AlipayGateway(AlipayGatewayConfig alipayConfig) {
        this.alipayConfig = alipayConfig;
        this.tradeApi = new AlipayTradeApi(buildApiClient(alipayConfig));
    }

    private static ApiClient buildApiClient(AlipayGatewayConfig config) {
        ApiClient client = com.alipay.v3.Configuration.getDefaultApiClient();
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setServerUrl(config.getGateway());
        alipayConfig.setAppId(config.getAppId());
        alipayConfig.setPrivateKey(config.getPrivateKey());
        alipayConfig.setAlipayPublicKey(config.getPublicKey());
        try {
            client.setAlipayConfig(alipayConfig);
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
        BigDecimal forceAmount = alipayConfig.getForceAmount();
        if (forceAmount != null) {
            log.warn("开发环境：将订单 {} 的交易金额从 {} 覆盖为固定值 {}",
                    payment.getOrderNo(), payment.getTotalPrice(), forceAmount);
            model.totalAmount(forceAmount.toPlainString());
        }
        // 支付宝异步支付回调地址（notify_url）：非空才设置，空则不传给支付宝
        String notifyUrl = alipayConfig.getNotifyUrl();
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
     * <p>subject / op_app_id / product_code 为支付宝渠道专属参数，由网关自身配置填充。</p>
     */
    private AlipayTradeCreateModel toAlipayTradeCreateModel(Payment payment) {
        return new AlipayTradeCreateModel()
                .outTradeNo(payment.getOrderNo())
                .totalAmount(payment.getTotalPrice().toPlainString())
                .productCode(alipayConfig.getProductCode())
                .subject(alipayConfig.getSubject())
                .opAppId(alipayConfig.getAppId())
                .buyerOpenId(payment.getCustomerOpenId());
    }
}
