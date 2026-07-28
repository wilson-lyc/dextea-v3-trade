package cn.dextea.trade.order.infrastructure.persistence.po;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusLogPO {
    private Long id;
    private Long orderId;
    private Integer fromStatus;
    private Integer toStatus;
    private String event;
    private String operator;
    private Integer version;
    private LocalDateTime createdAt;
}
