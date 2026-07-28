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
public class ProductImagePO {
    private Long productId;
    private Long imageId;
    private Integer type;
    private Integer sort;
    private LocalDateTime createdAt;
}
