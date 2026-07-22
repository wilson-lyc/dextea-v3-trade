package cn.dextea.trade.config;

import com.alipay.v3.ApiClient;
import com.alipay.v3.ApiException;
import com.alipay.v3.util.model.AlipayConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
