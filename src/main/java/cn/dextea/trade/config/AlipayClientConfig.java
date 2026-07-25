package cn.dextea.trade.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import cn.dextea.trade.factory.AlipayClientFactory;

import java.math.BigDecimal;

@Configuration
@ConfigurationProperties(prefix = "alipay")
@Getter
@Setter
public class AlipayClientConfig {

    private String gateway = "https://openapi.alipay.com";

    private String appId;

    private String privateKey;

    private String publicKey;

    private String subject = "德贤茶庄订单";

    private String productCode = "JSAPI_PAY";

    private String notifyUrl;

    @Getter(AccessLevel.NONE)
    private String forceAmount;

    public BigDecimal getForceAmount() {
        return AlipayClientFactory.parseAmount(forceAmount);
    }
}
