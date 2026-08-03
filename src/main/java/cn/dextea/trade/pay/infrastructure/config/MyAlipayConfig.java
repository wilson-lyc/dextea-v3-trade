package cn.dextea.trade.pay.infrastructure.config;

import com.alipay.v3.ApiClient;
import com.alipay.v3.Configuration;
import com.alipay.v3.util.model.AlipayConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AlipayProperties.class)
public class MyAlipayConfig {

    @Bean
    public ApiClient alipayApiClient(AlipayProperties properties) {
        ApiClient apiClient = Configuration.getDefaultApiClient();
        AlipayConfig config = new AlipayConfig();
        config.setServerUrl(properties.getServerUrl());
        config.setAppId(properties.getAppId());
        config.setPrivateKey(properties.getPrivateKey());
        config.setAlipayPublicKey(properties.getAlipayPublicKey());
        apiClient.setAlipayConfig(config);
        return apiClient;
    }
}
