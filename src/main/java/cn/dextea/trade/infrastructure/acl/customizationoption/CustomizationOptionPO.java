package cn.dextea.trade.infrastructure.acl.customizationoption;

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

    private Long customizationId;

    private String name;

    private BigDecimal price;

    private Integer sort;

    private Integer status;

    private Long ingredientId;

    private BigDecimal ingredientQuantity;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
