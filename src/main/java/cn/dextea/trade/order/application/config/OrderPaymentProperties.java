package cn.dextea.trade.order.application.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * 订单支付相关配置，配置前缀为 order。
 *
 * <p>支付超时时长由本系统持有：下单时以「当前时间 + 超时时长」计算出确切的支付过期
 * 时间点（{@code pay_expire_at}），落库并透传给支付宝（time_expire），保证两端关单
 * 时刻一致，前端亦可据此做倒计时。</p>
 */
@Data
@Validated
@Component
@ConfigurationProperties(prefix = "order")
public class OrderPaymentProperties {

    /**
     * 支付超时时长（分钟）。支付宝 time_expire 要求距当前时间至少 1 分钟、最长 15 天，
     * 此处限定 1 ~ 21600（15 天）分钟。
     */
    @Min(value = 1, message = "order.pay-timeout-minutes 不能小于 1 分钟")
    @Max(value = 21600, message = "order.pay-timeout-minutes 不能超过 15 天")
    private int payTimeoutMinutes = 15;

    public Duration payTimeout() {
        return Duration.ofMinutes(payTimeoutMinutes);
    }
}
