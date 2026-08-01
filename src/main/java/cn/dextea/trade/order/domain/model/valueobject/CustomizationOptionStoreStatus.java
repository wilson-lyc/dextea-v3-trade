package cn.dextea.trade.order.domain.model.valueobject;

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

    public Optional<CustomizationOptionStoreStatusCode> getStoreStatus() {
        return status == null ? Optional.empty() : Optional.of(CustomizationOptionStoreStatusCode.of(status));
    }

    public boolean isAvailable() {
        return status != null && CustomizationOptionStoreStatusCode.AVAILABLE.getCode() == status;
    }
}
