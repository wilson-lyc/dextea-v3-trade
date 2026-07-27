package cn.dextea.trade.pay.application.command;

import cn.dextea.trade.pay.domain.enums.PlatformEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建支付命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentCommand {

    /** 商户订单号 */
    private String orderNo;

    /** 订单总金额（元） */
    private BigDecimal totalPrice;

    /** 顾客唯一标识 */
    private String customerOpenId;

    /** 订单商品总数量 */
    private Integer totalQuantity;

    /** 支付平台 */
    private PlatformEnum platform;

    /** 支付过期时间点（由订单系统计算，透传给支付渠道保证两端关单时刻一致） */
    private LocalDateTime payExpireAt;
}
