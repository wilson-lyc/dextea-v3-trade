package cn.dextea.trade.pay.domain.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

/**
 * 支付领域对象
 */
@Value
@Builder
public class Payment {

    /** 商户订单号 */
    String orderNo;

    /** 订单总金额（元） */
    BigDecimal totalPrice;

    /** 顾客在支付渠道的唯一标识 */
    String customerOpenId;

    /** 订单商品总数量 */
    Integer totalQuantity;

    /** 支付方式 */
    PaymentMethodEnum paymentMethod;
}
