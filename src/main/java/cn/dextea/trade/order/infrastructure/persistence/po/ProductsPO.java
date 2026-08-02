package cn.dextea.trade.order.infrastructure.persistence.po;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductsPO {
    private Long id;
    private String name;
    private String brief;
    private String description;
    private Integer status;
    private BigDecimal price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
