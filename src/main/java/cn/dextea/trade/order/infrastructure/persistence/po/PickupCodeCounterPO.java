package cn.dextea.trade.order.infrastructure.persistence.po;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PickupCodeCounterPO {
    private Long storeId;
    private LocalDate date;
    private Integer dailyCount;
}
