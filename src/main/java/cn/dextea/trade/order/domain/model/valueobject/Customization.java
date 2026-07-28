package cn.dextea.trade.order.domain.model.valueobject;
import cn.dextea.trade.order.domain.enums.CustomizationStatusEnum;
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
public class Customization {
    private Long id;
    private Long productId;
    private String name;
    private Integer sort;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public Optional<CustomizationStatusEnum> getStatus() {
        return status == null ? Optional.empty() : Optional.of(CustomizationStatusEnum.of(status));
    }
    public boolean isGloballyAvailable() {
        return status != null && CustomizationStatusEnum.ACTIVE.getCode() == status;
    }
}
