package cn.dextea.trade.order.infrastructure.gateway.impl;

import cn.dextea.trade.catalog.api.client.CatalogClient;
import cn.dextea.trade.catalog.domain.model.aggregate.Customer;
import cn.dextea.trade.order.domain.gateway.CustomerGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerGatewayImpl implements CustomerGateway {
    private final CatalogClient catalogClient;

    @Override
    public Customer findCustomer(Long id) {
        return catalogClient.findCustomer(id);
    }
}
