package cn.dextea.trade.order.infrastructure.gateway.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 客制化选项表（customization_options）持久化对象：仅基础设施层可见。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomizationOptionPO {

    private Long id;

    private Long customizationId;

    private String name;

    private BigDecimal price;

    private Integer status;
}
