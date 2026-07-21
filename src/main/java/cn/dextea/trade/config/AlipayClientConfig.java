package cn.dextea.trade.config;

import com.alipay.v3.ApiClient;
import com.alipay.v3.Configuration;
import com.alipay.v3.util.model.AlipayConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 初始化支付宝 v3 SDK 的全局 ApiClient（基于 alipay-sdk-java-v3）。
 * 在应用启动时配置一次，后续 AlipayTradeApi 默认使用此客户端。
 */
@Configuration
public class AlipayClientConfig {

    @Bean
    public ApiClient alipayApiClient(AlipayProperties props) {
        ApiClient client = Configuration.getDefaultApiClient();
        AlipayConfig config = new AlipayConfig();
        config.setServerUrl(props.getServerUrl());
        config.setAppId(props.getAppId());
        config.setPrivateKey(props.getPrivateKey());
        config.setAlipayPublicKey(props.getAlipayPublicKey());
        client.setAlipayConfig(config);
        return client;
    }
}
