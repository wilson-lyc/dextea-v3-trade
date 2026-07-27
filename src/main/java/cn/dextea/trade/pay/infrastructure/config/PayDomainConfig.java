package cn.dextea.trade.pay.infrastructure.config;

import cn.dextea.trade.pay.domain.gateway.PaymentResultSyncGateway;
import cn.dextea.trade.pay.domain.service.PaymentDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付域组合根：将「无 Spring 注解」的领域服务装配为 Spring Bean。
 *
 * <p>领域层保持对框架零依赖，Bean 的注册放在这个基础设施层的配置里完成。</p>
 */
@Configuration
public class PayDomainConfig {

    @Bean
    public PaymentDomainService paymentDomainService(PaymentResultSyncGateway paymentResultSyncGateway) {
        return new PaymentDomainService(paymentResultSyncGateway);
    }
}
