package cn.dextea.trade.pay.infrastructure.config;
import cn.dextea.trade.pay.domain.gateway.PaymentResultSyncGateway;
import cn.dextea.trade.pay.domain.service.PaymentDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class PayDomainConfig {
    @Bean
    public PaymentDomainService paymentDomainService(PaymentResultSyncGateway paymentResultSyncGateway) {
        return new PaymentDomainService(paymentResultSyncGateway);
    }
}
