package cn.dextea.trade.pay.infrastructure.config;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import java.math.BigDecimal;
@Slf4j
@Data
@Validated
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayPaymentConfig {
    private String serverUrl = "https://openapi.alipay.com";
    @NotBlank(message = "alipay.app-id 必须配置")
    private String appId;
    @NotBlank(message = "alipay.private-key 必须配置")
    @ToString.Exclude
    private String privateKey;
    @NotBlank(message = "alipay.alipay-public-key 必须配置")
    @ToString.Exclude
    private String alipayPublicKey;
    private String subject = "德贤茶庄订单";
    @NotBlank(message = "alipay.notify-url 必须配置，用于接收支付宝异步回调")
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
