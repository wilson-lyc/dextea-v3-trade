package cn.dextea.trade.order.infrastructure.config;
import cn.dextea.trade.order.domain.gateway.CustomizationGateway;
import cn.dextea.trade.order.domain.gateway.CustomerGateway;
import cn.dextea.trade.order.domain.gateway.OrderLockGateway;
import cn.dextea.trade.order.domain.gateway.PaymentClientGateway;
import cn.dextea.trade.order.domain.gateway.PickupCodeGeneratorGateway;
import cn.dextea.trade.order.domain.gateway.ProductGateway;
import cn.dextea.trade.order.domain.gateway.StoreGateway;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.order.domain.service.OrderPlacementDomainService;
import cn.dextea.trade.order.domain.service.OrderStatusDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
            OrderLockGateway orderLockGateway,
            PickupCodeGeneratorGateway pickupCodeGeneratorGateway) {
        return new OrderStatusDomainService(orderRepository, orderLockGateway, pickupCodeGeneratorGateway);
    }
}
