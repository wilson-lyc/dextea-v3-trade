package cn.dextea.trade.factory;

import com.alipay.v3.ApiClient;
import com.alipay.v3.ApiException;
import com.alipay.v3.util.model.AlipayConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.dextea.trade.config.AlipayClientConfig;

import java.math.BigDecimal;

@Slf4j
@Configuration
public class AlipayClientFactory {

    @Bean
    public ApiClient createClient(AlipayClientConfig config) {
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

    public static BigDecimal parseAmount(String value) {
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
