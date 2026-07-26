package cn.dextea.trade.order.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单概要视图（领域值对象），供应用层映射为对外概要响应。
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryView {

    private String storeName;

    private LocalDateTime orderTime;

    private Integer tradeStatus;

    private String tradeStatusDesc;

    private Integer makingStatus;

    private String makingStatusDesc;

    private BigDecimal totalPrice;

    private Integer totalQuantity;

    private List<String> coverUrls;
}
