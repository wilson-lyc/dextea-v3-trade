package cn.dextea.trade.order.infrastructure.gateway.po;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
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
