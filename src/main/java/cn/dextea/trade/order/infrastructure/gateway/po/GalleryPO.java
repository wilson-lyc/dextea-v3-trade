package cn.dextea.trade.order.infrastructure.gateway.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 图库表（gallery）持久化对象：仅基础设施层可见。
 *
 * <p>领域层不感知图库模型，仅消费清洗后的封面 URL。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GalleryPO {

    private Long id;

    private String url;

    private String objectKey;

    private LocalDateTime createdAt;

    private String name;
}
