package cn.dextea.trade.order.infrastructure.gateway.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 商品图片关联表（product_images）持久化对象：仅基础设施层可见。
 *
 * <p>图库表结构对领域层完全隔离，领域层只消费清洗后的「productId → 封面」映射。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImagePO {

    private Long productId;

    private Long imageId;

    private Integer type;

    private Integer sort;

    private LocalDateTime createdAt;
}
