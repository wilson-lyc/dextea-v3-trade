package cn.dextea.trade.order.application.config;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import java.time.Duration;
@Data
@Validated
@Component
@ConfigurationProperties(prefix = "order")
public class OrderPaymentProperties {
    @Min(value = 1, message = "order.pay-timeout-minutes 不能小于 1 分钟")
    @Max(value = 21600, message = "order.pay-timeout-minutes 不能超过 15 天")
    private int payTimeoutMinutes = 15;
    public Duration payTimeout() {
        return Duration.ofMinutes(payTimeoutMinutes);
    }
}
