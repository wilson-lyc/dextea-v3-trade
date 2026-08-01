package cn.dextea.trade.order.domain.model.entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusLog {
    private Long id;
    private Long orderId;
    private Integer fromStatus;
    private Integer toStatus;
    private String event;
    private String operator;
    private Integer version;
    private LocalDateTime createdAt;
}
