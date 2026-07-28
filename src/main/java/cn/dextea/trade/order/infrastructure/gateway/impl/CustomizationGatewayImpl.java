package cn.dextea.trade.order.infrastructure.gateway.impl;
import cn.dextea.trade.order.domain.gateway.CustomizationGateway;
import cn.dextea.trade.order.domain.model.valueobject.Customization;
import cn.dextea.trade.order.domain.model.valueobject.CustomizationOption;
import cn.dextea.trade.order.domain.model.valueobject.CustomizationOptionStoreStatus;
import cn.dextea.trade.order.infrastructure.gateway.mapper.CatalogMapper;
import cn.dextea.trade.order.infrastructure.gateway.translator.CustomizationTranslator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
@Component
@RequiredArgsConstructor
public class CustomizationGatewayImpl implements CustomizationGateway {
    private final CatalogMapper catalogMapper;
    @Override
    public List<Customization> findCustomizations(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return CustomizationTranslator.toCustomizations(catalogMapper.selectCustomizationsByIds(ids));
    }
    @Override
    public List<CustomizationOption> findOptions(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return CustomizationTranslator.toOptions(catalogMapper.selectCustomizationOptionsByIds(ids));
    }
    @Override
    public List<CustomizationOptionStoreStatus> findOptionStoreStatus(List<Long> optionIds, Long storeId) {
        if (optionIds == null || optionIds.isEmpty()) {
            return List.of();
        }
        return CustomizationTranslator.toOptionStoreStatusList(
                catalogMapper.selectOptionStoreStatusByOptionIdsAndStoreId(optionIds, storeId));
    }
}
