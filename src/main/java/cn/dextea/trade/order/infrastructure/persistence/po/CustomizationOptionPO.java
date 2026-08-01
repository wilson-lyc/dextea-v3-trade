package cn.dextea.trade.order.infrastructure.persistence.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomizationOptionPO {
    private Long id;
    private Long itemId;
    private String name;
    private BigDecimal price;
    private Integer sort;
    private Integer status;
    private Long ingredientId;
    private Double ingredientQuantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
