package cn.dextea.trade.order.infrastructure.config;

import cn.dextea.trade.order.domain.factory.OrderNumberFactory;
import cn.dextea.trade.order.domain.factory.PickupCodeFactory;
import cn.dextea.trade.order.domain.port.OrderLock;
import cn.dextea.trade.order.domain.port.OrderNumberGenerator;
import cn.dextea.trade.order.domain.port.PickupCodeGenerator;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.order.domain.service.OrderPlacementDomainService;
import cn.dextea.trade.order.domain.service.OrderStatusDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderDomainConfig {

    @Bean
    public OrderPlacementDomainService orderPlacementDomainService() {
        return new OrderPlacementDomainService();
    }

    @Bean
    public OrderNumberFactory orderNumberFactory(OrderNumberGenerator generator) {
        return new OrderNumberFactory(generator);
    }

    @Bean
    public PickupCodeFactory pickupCodeFactory(PickupCodeGenerator generator) {
        return new PickupCodeFactory(generator);
    }

    @Bean
    public OrderStatusDomainService orderStatusDomainService(
            OrderRepository orderRepository,
            OrderLock orderLock,
            PickupCodeFactory pickupCodeFactory) {
        return new OrderStatusDomainService(orderRepository, orderLock, pickupCodeFactory);
    }
}
