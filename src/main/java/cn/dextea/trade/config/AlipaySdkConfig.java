package cn.dextea.trade.config;

import com.alipay.v3.ApiClient;
import com.alipay.v3.ApiException;
import com.alipay.v3.util.model.AlipayConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "alipay")
@Getter
@Setter
public class AlipaySdkConfig {

    /** 支付宝网关地址 */
    private String serverUrl = "https://openapi.alipay.com";

    /** 应用 AppId */
    private String appId;

    /** 应用私钥 */
    private String privateKey;

    /** 支付宝公钥 */
    private String alipayPublicKey;

    /** 订单标题前缀 */
    private String subject = "德贤茶庄订单";

    /**
     * 开发/测试环境下强制使用的固定订单金额（元）。
     * 当该值非空时，创建支付宝交易会把订单总额覆盖为此固定值，
     * 避免开发联调或沙箱环境误产生真实交易金额。生产环境应置空以使用真实金额。
     * 对应配置项 {@code alipay.force-amount} / 环境变量 {@code ALIPAY_FORCE_AMOUNT}。
     */
    private BigDecimal forceAmount;

    @Bean
    public ApiClient alipayApiClient() {
        ApiClient client = com.alipay.v3.Configuration.getDefaultApiClient();
        AlipayConfig config = new AlipayConfig();
        config.setServerUrl(serverUrl);
        config.setAppId(appId);
        config.setPrivateKey(privateKey);
        config.setAlipayPublicKey(alipayPublicKey);
        try {
            client.setAlipayConfig(config);
        } catch (ApiException e) {
            throw new IllegalStateException("初始化支付宝 SDK 配置失败", e);
        }
        return client;
    }
}
