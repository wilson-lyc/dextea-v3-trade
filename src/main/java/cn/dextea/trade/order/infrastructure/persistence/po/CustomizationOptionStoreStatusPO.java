package cn.dextea.trade.order.infrastructure.persistence.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomizationOptionStoreStatusPO {
    private Long optionId;
    private Long storeId;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
