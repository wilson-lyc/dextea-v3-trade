package cn.dextea.trade.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 订单轻量状态（应用层 DTO），供前端轮询交易结果，由接口层映射为状态响应。
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusDTO {

    private Long orderId;

    private String orderNo;

    private String tradeNo;

    private Integer tradeStatus;

    private String tradeStatusDesc;

    private Integer makingStatus;

    private String makingStatusDesc;

    /** 支付过期时间点（待支付订单前端可据此做倒计时） */
    private LocalDateTime payExpireAt;

    /** 支付完成时间 */
    private LocalDateTime paidAt;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;

    /** 交易是否已达终态，前端据此决定是否停止轮询 */
    private Boolean terminal;
}
