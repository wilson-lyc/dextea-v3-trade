package cn.dextea.trade.console.infrastructure.persistence.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiTokenPO {
    private Long id;
    private String token;
    private String name;
    private boolean enabled;
    private LocalDateTime expireAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
