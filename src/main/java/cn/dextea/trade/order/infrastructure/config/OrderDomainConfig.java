package cn.dextea.trade.order.infrastructure.config;

import cn.dextea.trade.order.domain.gateway.CustomizationGateway;
import cn.dextea.trade.order.domain.gateway.CustomerGateway;
import cn.dextea.trade.order.domain.gateway.OrderLockGateway;
import cn.dextea.trade.order.domain.gateway.PaymentClientGateway;
import cn.dextea.trade.order.domain.gateway.ProductGateway;
import cn.dextea.trade.order.domain.gateway.StoreGateway;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.order.domain.service.OrderPlacementDomainService;
import cn.dextea.trade.order.domain.service.OrderStatusDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 订单域组合根：将「无 Spring 注解」的领域服务装配为 Spring Bean。
 *
 * <p>领域层保持对框架零依赖，Bean 的注册放在这个基础设施层的配置里完成。</p>
 */
@Configuration
public class OrderDomainConfig {

    @Bean
    public OrderPlacementDomainService orderPlacementDomainService(
            ProductGateway productGateway,
            CustomizationGateway customizationGateway,
            StoreGateway storeGateway,
            CustomerGateway customerGateway,
            PaymentClientGateway paymentClientGateway) {
        return new OrderPlacementDomainService(
                productGateway, customizationGateway, storeGateway, customerGateway, paymentClientGateway);
    }

    @Bean
    public OrderStatusDomainService orderStatusDomainService(
            OrderRepository orderRepository,
            OrderLockGateway orderLockGateway) {
        return new OrderStatusDomainService(orderRepository, orderLockGateway);
    }
}
