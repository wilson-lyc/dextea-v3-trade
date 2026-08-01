package cn.dextea.trade.order.domain.port;

import cn.dextea.trade.order.domain.model.valueobject.Customization;
import cn.dextea.trade.order.domain.model.valueobject.CustomizationOption;
import cn.dextea.trade.order.domain.model.valueobject.CustomizationOptionStoreStatus;

import java.util.List;

public interface CustomizationRepository {

    List<Customization> findCustomizations(List<Long> ids);

    List<CustomizationOption> findOptions(List<Long> ids);

    List<CustomizationOptionStoreStatus> findOptionStoreStatus(List<Long> optionIds, Long storeId);
}
