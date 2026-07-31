package cn.dextea.trade.catalog.infrastructure.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomizationPO {
    private Long id;
    private Long productId;
    private String name;
    private Integer status;
}
