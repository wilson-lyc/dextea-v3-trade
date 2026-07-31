package cn.dextea.trade.catalog.infrastructure.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomizationOptionStoreStatusPO {
    private Long customizationOptionId;
    private Long storeId;
    private Integer status;
}
