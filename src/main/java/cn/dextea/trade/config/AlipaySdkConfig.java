package cn.dextea.trade.config;

import com.alipay.v3.ApiClient;
import com.alipay.v3.ApiException;
import com.alipay.v3.util.model.AlipayConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * 支付宝配置的唯一入口。
 *
 * <p>所有支付宝相关配置项（网关、AppId、密钥、标题、产品码、强制金额、回调地址）都通过 Spring 的属性占位符
 * {@code ${alipay.*:默认值}} 注入，因此支持两种注入方式：
 * <ul>
 *     <li><b>环境变量</b>：借助 Spring 宽松绑定，{@code alipay.openapi-gateway} 对应 {@code ALIPAY_OPENAPI_GATEWAY}，
 *         其余同理（详见 {@code .env.example}）。</li>
 *     <li><b>Nacos</b>：在 {@code dextea-trade.yaml} 中以 {@code alipay:} 配置块下发即可，与本地 {@code application.yaml} 结构一致。</li>
 * </ul>
 *
 * <p>对应的环境变量名如下，与 {@code .env.example} 保持一致：
 * <ul>
 *     <li>{@code ALIPAY_OPENAPI_GATEWAY} → {@code alipay.openapi-gateway}</li>
 *     <li>{@code ALIPAY_APP_ID} → {@code alipay.app-id}</li>
 *     <li>{@code ALIPAY_PRIVATE_KEY} → {@code alipay.private-key}</li>
 *     <li>{@code ALIPAY_PUBLIC_KEY} → {@code alipay.public-key}</li>
 *     <li>{@code ALIPAY_SUBJECT} → {@code alipay.subject}</li>
 *     <li>{@code ALIPAY_PRODUCT_CODE} → {@code alipay.product-code}</li>
 *     <li>{@code ALIPAY_FORCE_AMOUNT} → {@code alipay.force-amount}</li>
 *     <li>{@code ALIPAY_NOTIFY_URL} → {@code alipay.notify-url}</li>
 * </ul>
 */
@Slf4j
@Configuration
@Getter
public class AlipaySdkConfig {

    /** 支付宝网关地址，对应 {@code alipay.openapi-gateway} / {@code ALIPAY_OPENAPI_GATEWAY}，默认正式环境网关 */
    @Value("${alipay.openapi-gateway:https://openapi.alipay.com}")
    private String gateway;

    /** 应用 AppId，对应 {@code alipay.app-id} / {@code ALIPAY_APP_ID}（使用支付宝支付时必填） */
    @Value("${alipay.app-id:#{null}}")
    private String appId;

    /** 应用私钥，对应 {@code alipay.private-key} / {@code ALIPAY_PRIVATE_KEY}（使用支付宝支付时必填，多行内容需双引号包裹） */
    @Value("${alipay.private-key:#{null}}")
    private String privateKey;

    /** 支付宝公钥，对应 {@code alipay.public-key} / {@code ALIPAY_PUBLIC_KEY}（使用支付宝支付时必填） */
    @Value("${alipay.public-key:#{null}}")
    private String publicKey;

    /** 订单标题前缀，对应 {@code alipay.subject} / {@code ALIPAY_SUBJECT} */
    @Value("${alipay.subject:德贤茶庄订单}")
    private String subject;

    /** 支付产品码，对应 {@code alipay.product-code} / {@code ALIPAY_PRODUCT_CODE} */
    @Value("${alipay.product-code:JSAPI_PAY}")
    private String productCode;

    /** 开发/测试环境强制金额（元）原始字符串，对应 {@code alipay.force-amount} / {@code ALIPAY_FORCE_AMOUNT} */
    @Value("${alipay.force-amount:0.01}")
    private String forceAmountRaw;

    /**
     * 支付宝异步支付回调地址（notify_url），对应 {@code alipay.notify-url} / {@code ALIPAY_NOTIFY_URL}。
     * 为空则不设置，非空时创建交易会作为异步通知地址传给支付宝。
     */
    @Value("${alipay.notify-url:#{null}}")
    private String notifyUrl;

    /**
     * 开发/测试环境下强制使用的固定订单金额（元）。
     * 非空时将覆盖真实订单金额，避免开发联调或沙箱环境误产生真实交易；生产环境置空以使用真实金额。
     */
    public BigDecimal getForceAmount() {
        return parseAmount(forceAmountRaw);
    }

    @Bean
    public ApiClient alipayApiClient() {
        ApiClient client = com.alipay.v3.Configuration.getDefaultApiClient();
        AlipayConfig config = new AlipayConfig();
        config.setServerUrl(gateway);
        config.setAppId(appId);
        config.setPrivateKey(privateKey);
        config.setAlipayPublicKey(publicKey);
        try {
            client.setAlipayConfig(config);
        } catch (ApiException e) {
            throw new IllegalStateException("初始化支付宝 SDK 配置失败", e);
        }
        return client;
    }

    /**
     * 将金额字符串解析为 {@link BigDecimal}；为空或非法时返回 {@code null}。
     */
    private static BigDecimal parseAmount(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            log.warn("支付宝强制金额 alipay.force-amount 值非法，已忽略：{}", value);
            return null;
        }
    }
}
