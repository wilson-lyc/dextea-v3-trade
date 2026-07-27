package cn.dextea.trade.order.domain.model.valueobject;

import cn.dextea.trade.order.domain.enums.ProductStoreStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 商品门店维度状态只读快照值对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStoreStatus {

    private Long productId;

    private Long storeId;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Optional<ProductStoreStatusEnum> getStoreStatus() {
        return status == null ? Optional.empty() : Optional.of(ProductStoreStatusEnum.of(status));
    }

    public boolean isAvailable() {
        return status != null && ProductStoreStatusEnum.AVAILABLE.getCode() == status;
    }
}
