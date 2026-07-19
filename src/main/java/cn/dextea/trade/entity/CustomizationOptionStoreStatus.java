package cn.dextea.trade.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomizationOptionStoreStatus {

    private Long customizationOptionId;

    private Long storeId;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
