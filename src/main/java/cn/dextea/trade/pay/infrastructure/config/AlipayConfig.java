package cn.dextea.trade.pay.infrastructure.config;

import com.alipay.v3.ApiClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AlipayProperties.class)
public class AlipayConfig {

    @Bean
    public ApiClient alipayApiClient(AlipayProperties properties) {
        ApiClient apiClient = com.alipay.v3.Configuration.getDefaultApiClient();
        com.alipay.v3.util.model.AlipayConfig config = new com.alipay.v3.util.model.AlipayConfig();
        config.setServerUrl(properties.getServerUrl());
        config.setAppId(properties.getAppId());
        config.setPrivateKey(properties.getPrivateKey());
        config.setAlipayPublicKey(properties.getAlipayPublicKey());
        apiClient.setAlipayConfig(config);
        return apiClient;
    }
}
