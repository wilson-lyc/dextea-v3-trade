package cn.dextea.trade.order.infrastructure.persistence.po;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductStoreStatusPO {
    private Long productId;
    private Long storeId;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
