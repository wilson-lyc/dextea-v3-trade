package cn.dextea.trade.catalog.infrastructure.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStoreStatusPO {
    private Long productId;
    private Long storeId;
    private Integer status;
}
