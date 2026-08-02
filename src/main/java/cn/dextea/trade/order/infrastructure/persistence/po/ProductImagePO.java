package cn.dextea.trade.order.infrastructure.persistence.po;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductImagePO {
    private Long productId;
    private Long imageId;
    private Integer type;
    private Integer sort;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
