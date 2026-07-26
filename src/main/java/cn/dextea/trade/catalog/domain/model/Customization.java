package cn.dextea.trade.catalog.domain.model;

import cn.dextea.trade.catalog.domain.enums.CustomizationStatusEnum;
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

    /** 是否处于全局可用（激活）状态。 */
    public boolean isGloballyAvailable() {
        return status != null && CustomizationStatusEnum.ACTIVE.getCode() == status;
    }
}
