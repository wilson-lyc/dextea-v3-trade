package cn.dextea.trade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCalculateResponse {

    private OrderCalculateUnavailable unavailable;

    private Integer totalQuantity;

    private BigDecimal totalPrice;
}
