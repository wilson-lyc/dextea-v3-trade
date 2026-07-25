package cn.dextea.trade.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import cn.dextea.trade.factory.AlipayClientFactory;

import java.math.BigDecimal;

@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayClientConfig {

    private String gateway = "https://openapi.alipay.com";

    private String appId;

    @ToString.Exclude
    private String privateKey;

    @ToString.Exclude
    private String publicKey;

    private String subject = "德贤茶庄订单";

    private String productCode = "JSAPI_PAY";

    private String notifyUrl;

    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private String forceAmount;

    public BigDecimal getForceAmount() {
        return AlipayClientFactory.parseAmount(forceAmount);
    }
}
