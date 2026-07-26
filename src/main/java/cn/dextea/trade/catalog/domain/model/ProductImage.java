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
public class ProductImage {

    private Long productId;

    private Long imageId;

    private Integer type;

    private Integer sort;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
