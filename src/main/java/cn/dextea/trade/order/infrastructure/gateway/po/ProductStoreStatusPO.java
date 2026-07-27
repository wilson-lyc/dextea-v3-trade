package cn.dextea.trade.order.infrastructure.gateway.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品门店状态表（product_store_status）持久化对象：仅基础设施层可见。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStoreStatusPO {

    private Long productId;

    private Long storeId;

    private Integer status;
}
