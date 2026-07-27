package cn.dextea.trade.order.infrastructure.gateway.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 客制化项目表（customizations）持久化对象：仅基础设施层可见。
 */
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
