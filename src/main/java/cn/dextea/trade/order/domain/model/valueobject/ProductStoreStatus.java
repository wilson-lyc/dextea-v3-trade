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
public class ProductStoreStatus {
    private Long productId;
    private Long storeId;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Optional<ProductStoreStatusCode> getStoreStatus() {
        return status == null ? Optional.empty() : Optional.of(ProductStoreStatusCode.of(status));
    }

    public boolean isAvailable() {
        return status != null && ProductStoreStatusCode.AVAILABLE.getCode() == status;
    }
}
