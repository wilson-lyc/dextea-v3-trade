package cn.dextea.trade.order.infrastructure.persistence.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderStatusLogPO {
    private Long id;
    private String orderId;
    private Integer fromStatus;
    private Integer toStatus;
    private String event;
    private Integer version;
    private LocalDateTime createdAt;
}
