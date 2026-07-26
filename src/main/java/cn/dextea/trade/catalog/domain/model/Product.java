package cn.dextea.trade.catalog.domain.model;

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
public class Product {

    private Long id;

    private String name;

    private String brief;

    private String description;

    private Integer status;

    private BigDecimal price;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
