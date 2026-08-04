package cn.dextea.trade.order.infrastructure.persistence.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StorePO {
    private Long id;
    private String name;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
