package cn.dextea.trade.order.infrastructure.gateway.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 商品表（products）持久化对象：仅基础设施层可见，由 Translator 清洗为领域值对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPO {

    private Long id;

    private String name;

    private Integer status;

    private BigDecimal price;
}
