package cn.dextea.trade.order.domain.model.valueobject;
import cn.dextea.trade.order.domain.enums.CustomizationOptionGlobalStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomizationOption {
    private Long id;
    private Long customizationId;
    private String name;
    private BigDecimal price;
    private Integer sort;
    private Integer status;
    private Long ingredientId;
    private BigDecimal ingredientQuantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public Optional<CustomizationOptionGlobalStatusEnum> getGlobalStatus() {
        return status == null ? Optional.empty() : Optional.of(CustomizationOptionGlobalStatusEnum.of(status));
    }
    public boolean isGloballyAvailable() {
        return status != null && CustomizationOptionGlobalStatusEnum.ACTIVE.getCode() == status;
    }
    public boolean isAvailableInStore(CustomizationOptionStoreStatus storeStatus) {
        return isGloballyAvailable() && storeStatus != null && storeStatus.isAvailable();
    }
}
