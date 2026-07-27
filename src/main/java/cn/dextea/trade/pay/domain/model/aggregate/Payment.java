package cn.dextea.trade.pay.domain.model.aggregate;

import cn.dextea.trade.pay.domain.enums.PlatformEnum;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付领域对象
 */
@Value
@Builder
public class Payment {

    /** 支付平台 */
    PlatformEnum platform;

    /** 商户订单号 */
    String orderNo;

    /** 顾客在支付渠道的唯一标识 */
    String customerOpenId;

    /** 订单商品总数量 */
    Integer totalQuantity;

    /** 订单总金额（元） */
    BigDecimal totalPrice;

    /** 支付过期时间点（订单系统计算的绝对时间，透传给支付渠道保证两端关单时刻一致） */
    LocalDateTime payExpireAt;
}
