package cn.dextea.trade.payment.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "alipay")
public class AlipayProperties {

    private String serverUrl;

    private String appId;

    private String privateKey;

    private String alipayPublicKey;

    private String subject;

    private String forceAmount;

    private String notifyUrl;
}
