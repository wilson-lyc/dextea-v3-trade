package cn.dextea.trade.catalog.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
}
