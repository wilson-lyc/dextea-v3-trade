package cn.dextea.trade.order.infrastructure.gateway.impl;

import cn.dextea.trade.catalog.api.client.CatalogClient;
import cn.dextea.trade.catalog.domain.model.valueobject.Customization;
import cn.dextea.trade.catalog.domain.model.valueobject.CustomizationOption;
import cn.dextea.trade.catalog.domain.model.valueobject.CustomizationOptionStoreStatus;
import cn.dextea.trade.order.domain.gateway.CustomizationGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomizationGatewayImpl implements CustomizationGateway {
    private final CatalogClient catalogClient;

    @Override
    public List<Customization> findCustomizations(List<Long> ids) {
        return catalogClient.findCustomizationMap(java.util.Set.copyOf(ids)).values().stream().toList();
    }

    @Override
    public List<CustomizationOption> findOptions(List<Long> ids) {
        return catalogClient.findOptionMap(java.util.Set.copyOf(ids)).values().stream().toList();
    }

    @Override
    public List<CustomizationOptionStoreStatus> findOptionStoreStatus(List<Long> optionIds, Long storeId) {
        return catalogClient.findOptionStoreStatusMap(optionIds, storeId).values().stream().toList();
    }
}
