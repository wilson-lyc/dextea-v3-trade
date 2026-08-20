package cn.dextea.trade.payment.infrastructure.config;

import cn.dextea.trade.shared.error.CommonErrorCode;
import cn.dextea.trade.shared.error.SystemException;
import com.alipay.v3.ApiClient;
import com.alipay.v3.ApiException;
import com.alipay.v3.util.model.AlipayConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AlipayProperties.class)
public class MyAlipayConfig {

    @Bean
    public ApiClient alipayApiClient(AlipayProperties properties) {
        ApiClient apiClient = com.alipay.v3.Configuration.getDefaultApiClient();
        AlipayConfig config = new AlipayConfig();
        config.setServerUrl(properties.getServerUrl());
        config.setAppId(properties.getAppId());
        config.setPrivateKey(properties.getPrivateKey());
        config.setAlipayPublicKey(properties.getAlipayPublicKey());
        try {
            apiClient.setAlipayConfig(config);
        } catch (ApiException e) {
            throw new SystemException(CommonErrorCode.SYSTEM_ERROR, "初始化支付宝 API 客户端失败", e);
        }
        return apiClient;
    }
}
