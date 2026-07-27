package cn.dextea.trade.order.domain.model.valueobject;

import cn.dextea.trade.order.domain.enums.ProductGlobalStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 商品只读快照值对象：由 {@code ProductGateway} 从外部商品表清洗后提供给订单领域。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private Long id;

    private String name;

    private String brief;

    private String description;

    private Integer status;

    private BigDecimal price;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Optional<ProductGlobalStatusEnum> getGlobalStatus() {
        return status == null ? Optional.empty() : Optional.of(ProductGlobalStatusEnum.of(status));
    }

    public boolean isOnShelf() {
        return status != null && ProductGlobalStatusEnum.ON_SHELF.getCode() == status;
    }

    public boolean isAvailableInStore(ProductStoreStatus storeStatus) {
        return isOnShelf() && storeStatus != null && storeStatus.isAvailable();
    }
}
