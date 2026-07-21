package cn.dextea.trade.entity;

import cn.dextea.trade.entity.enums.OrderStatus;
import cn.dextea.trade.entity.enums.PayMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private Long id;

    private String orderNo;

    private String tradeNo;

    private Long customerId;

    private Long storeId;

    private Integer status;

    private BigDecimal price;

    private Integer payMethod;

    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime paidAt;

    private LocalDateTime refundedAt;

    private LocalDateTime updatedAt;
}
