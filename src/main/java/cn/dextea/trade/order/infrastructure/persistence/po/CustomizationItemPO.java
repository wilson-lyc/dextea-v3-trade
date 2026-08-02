package cn.dextea.trade.order.infrastructure.persistence.po;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CustomizationItemPO {
    private Long id;
    private Long productId;
    private String name;
    private Integer sort;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
