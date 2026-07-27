package cn.dextea.trade.order.infrastructure.gateway.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 客制化选项门店状态表（customization_option_store_status）持久化对象：仅基础设施层可见。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomizationOptionStoreStatusPO {

    private Long customizationOptionId;

    private Long storeId;

    private Integer status;
}
