package cn.dextea.trade.order.infrastructure.persistence.repository;

import cn.dextea.trade.order.domain.model.valueobject.Customization;
import cn.dextea.trade.order.domain.model.valueobject.CustomizationOption;
import cn.dextea.trade.order.domain.model.valueobject.CustomizationOptionStoreStatus;
import cn.dextea.trade.order.domain.port.CustomizationRepository;
import cn.dextea.trade.order.infrastructure.persistence.mapper.ProductMapper;
import cn.dextea.trade.order.infrastructure.persistence.translator.CustomizationTranslator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomizationRepositoryImpl implements CustomizationRepository {

    private final ProductMapper productMapper;

    @Override
    public List<Customization> findCustomizations(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return CustomizationTranslator.toCustomizations(productMapper.selectCustomizationsByIds(ids));
    }

    @Override
    public List<CustomizationOption> findOptions(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return CustomizationTranslator.toOptions(productMapper.selectCustomizationOptionsByIds(ids));
    }

    @Override
    public List<CustomizationOptionStoreStatus> findOptionStoreStatus(List<Long> optionIds, Long storeId) {
        if (optionIds == null || optionIds.isEmpty()) {
            return List.of();
        }
        return CustomizationTranslator.toOptionStoreStatusList(
                productMapper.selectOptionStoreStatusByOptionIdsAndStoreId(optionIds, storeId));
    }
}
