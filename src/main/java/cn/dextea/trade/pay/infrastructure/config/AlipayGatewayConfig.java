package cn.dextea.trade.pay.infrastructure.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 支付宝网关配置
 * 配置前缀为 alipay。
 */
@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayGatewayConfig {

    private String gateway = "https://openapi.alipay.com";

    private String appId;

    @ToString.Exclude
    private String privateKey;

    @ToString.Exclude
    private String publicKey;

    private String subject = "德贤茶庄订单";

    private String notifyUrl;

    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private String forceAmount;

    public BigDecimal getForceAmount() {
        if (forceAmount == null || forceAmount.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(forceAmount.trim());
        } catch (NumberFormatException e) {
            log.warn("alipay.force-amount 值非法，已忽略：{}", forceAmount);
            return null;
        }
    }
}
