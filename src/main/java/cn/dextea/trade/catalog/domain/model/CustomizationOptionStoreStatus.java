package cn.dextea.trade.catalog.domain.model;

import cn.dextea.trade.catalog.domain.enums.CustomizationOptionStoreStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomizationOptionStoreStatus {

    private Long customizationOptionId;

    private Long storeId;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Optional<CustomizationOptionStoreStatusEnum> getStoreStatus() {
        return status == null ? Optional.empty() : Optional.of(CustomizationOptionStoreStatusEnum.of(status));
    }

    public boolean isAvailable() {
        return status != null && CustomizationOptionStoreStatusEnum.AVAILABLE.getCode() == status;
    }
}
