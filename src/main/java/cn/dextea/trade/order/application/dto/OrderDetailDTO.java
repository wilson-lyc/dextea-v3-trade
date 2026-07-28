package cn.dextea.trade.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情（应用层 DTO），供接口层映射为对外详情响应。
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDTO {

    private Long id;

    private String orderNo;

    private String tradeNo;

    private Integer tradeStatus;

    private String tradeStatusDesc;

    private Integer makingStatus;

    private String makingStatusDesc;

    /** 取餐码（支付成功后生成，如 "8011"；未支付为 null） */
    private String pickupCode;

    private BigDecimal totalPrice;

    private Integer totalQuantity;

    private Integer payMethod;

    private String payMethodDesc;

    private Integer diningMethod;

    private String diningMethodDesc;

    private String note;

    /** 支付过期时间点（系统计算并已同步支付宝），待支付订单前端可据此做倒计时 */
    private LocalDateTime payExpireAt;

    private LocalDateTime createdAt;

    private LocalDateTime paidAt;

    private LocalDateTime refundedAt;

    private LocalDateTime updatedAt;

    private StoreInfoDTO store;

    private List<OrderItemDTO> items;
}
