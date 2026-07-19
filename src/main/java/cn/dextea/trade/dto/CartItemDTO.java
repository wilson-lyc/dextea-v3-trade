package cn.dextea.trade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {

    private Integer id;

    private String skuId;

    private String name;

    private BigDecimal price;

    private Integer quantity;

    private String customization;
}
