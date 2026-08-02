package cn.dextea.trade.order.infrastructure.persistence.po;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GalleryPO {
    private Long id;
    private String url;
    private String objectKey;
    private String name;
    private LocalDateTime createdAt;
}
