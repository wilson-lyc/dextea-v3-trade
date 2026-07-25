package cn.dextea.trade.config;

import com.alipay.v3.ApiClient;
import com.alipay.v3.ApiException;
import com.alipay.v3.util.model.AlipayConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * 支付宝配置的唯一入口。
 *
 * <p>所有支付宝相关配置项（网关、AppId、密钥、标题、产品码、强制金额、回调地址）都在本类以字段形式统一定义，
 * 并通过 {@link #loadFromEnv()} 从环境变量读取赋值，便于后续接入 Nacos 等统一配置中心时只在此处扩展。
 *
 * <p>对应的环境变量名见各字段注解，与 {@code .env.example} 保持一致：
 * <ul>
 *     <li>{@code ALIPAY_OPENAPI_GATEWAY}</li>
 *     <li>{@code ALIPAY_APP_ID}</li>
 *     <li>{@code ALIPAY_PRIVATE_KEY}</li>
 *     <li>{@code ALIPAY_PUBLIC_KEY}</li>
 *     <li>{@code ALIPAY_SUBJECT}</li>
 *     <li>{@code ALIPAY_PRODUCT_CODE}</li>
 *     <li>{@code ALIPAY_FORCE_AMOUNT}</li>
 *     <li>{@code ALIPAY_NOTIFY_URL}</li>
 * </ul>
 */
@Slf4j
@Configuration
@Getter
public class AlipaySdkConfig {

    /** 支付宝网关地址，对应环境变量 {@code ALIPAY_OPENAPI_GATEWAY}，默认正式环境网关 */
    private final String gateway = getEnv("ALIPAY_OPENAPI_GATEWAY", "https://openapi.alipay.com");

    /** 应用 AppId，对应环境变量 {@code ALIPAY_APP_ID}（使用支付宝支付时必填） */
    private final String appId = getEnv("ALIPAY_APP_ID", null);

    /** 应用私钥，对应环境变量 {@code ALIPAY_PRIVATE_KEY}（使用支付宝支付时必填，多行内容需双引号包裹） */
    private final String privateKey = getEnv("ALIPAY_PRIVATE_KEY", null);

    /** 支付宝公钥，对应环境变量 {@code ALIPAY_PUBLIC_KEY}（使用支付宝支付时必填） */
    private final String publicKey = getEnv("ALIPAY_PUBLIC_KEY", null);

    /** 订单标题前缀，对应环境变量 {@code ALIPAY_SUBJECT} */
    private final String subject = getEnv("ALIPAY_SUBJECT", "德贤茶庄订单");

    /** 支付产品码，对应环境变量 {@code ALIPAY_PRODUCT_CODE} */
    private final String productCode = getEnv("ALIPAY_PRODUCT_CODE", "JSAPI_PAY");

    /**
     * 开发/测试环境下强制使用的固定订单金额（元），对应环境变量 {@code ALIPAY_FORCE_AMOUNT}。
     * 非空时将覆盖真实订单金额，避免开发联调或沙箱环境误产生真实交易；生产环境置空以使用真实金额。
     */
    private final BigDecimal forceAmount = parseAmount(getEnv("ALIPAY_FORCE_AMOUNT", "0.01"));

    /**
     * 支付宝异步支付回调地址（notify_url），对应环境变量 {@code ALIPAY_NOTIFY_URL}。
     * 为空则不设置，非空时创建交易会作为异步通知地址传给支付宝。
     */
    private final String notifyUrl = getEnv("ALIPAY_NOTIFY_URL", null);

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
     * 读取环境变量；当未设置或为空字符串时返回默认值。
     *
     * @param name         环境变量名
     * @param defaultValue 默认值（可为 {@code null}）
     * @return 环境变量值或默认值
     */
    private static String getEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    /**
     * 将环境变量中的金额字符串解析为 {@link BigDecimal}；为空或非法时返回 {@code null}。
     */
    private static BigDecimal parseAmount(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            log.warn("支付宝强制金额 ALIPAY_FORCE_AMOUNT 值非法，已忽略：{}", value);
            return null;
        }
    }
}
