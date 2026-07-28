package cn.dextea.trade.order.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 订单交易状态响应（前端轮询交易结果），仅含轻量状态字段与终态标记。
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusResponse {

    private Long orderId;

    private String orderNo;

    private String tradeNo;

    private Integer tradeStatus;

    private Integer makingStatus;

    /** 取餐码（支付成功后生成，如 "8011"；未支付为 null） */
    private String pickupCode;

    private LocalDateTime payExpireAt;

    private LocalDateTime paidAt;

    private LocalDateTime updatedAt;

    /** 交易是否已达终态，前端据此决定是否停止轮询 */
    private Boolean terminal;
}
