package cn.dextea.trade.order.domain.gateway;

import cn.dextea.trade.order.domain.model.valueobject.Customization;
import cn.dextea.trade.order.domain.model.valueobject.CustomizationOption;
import cn.dextea.trade.order.domain.model.valueobject.CustomizationOptionStoreStatus;

import java.util.List;

/**
 * 客制化网关：以只读快照形式向订单领域提供客制化项目、选项及其门店可用性数据。
 */
public interface CustomizationGateway {

    List<Customization> findCustomizations(List<Long> ids);

    List<CustomizationOption> findOptions(List<Long> ids);

    List<CustomizationOptionStoreStatus> findOptionStoreStatus(List<Long> optionIds, Long storeId);
}
