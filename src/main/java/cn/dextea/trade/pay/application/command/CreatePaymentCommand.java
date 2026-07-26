package cn.dextea.trade.pay.application.command;

import cn.dextea.trade.pay.domain.model.PaymentMethodEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 创建支付命令，渠道无关。
 *
 * <p>渠道专属参数（如支付宝 subject / op_app_id / product_code）由基础设施层网关自身配置填充，调用方无需感知。</p>
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

    /** 顾客在支付渠道的唯一标识（如支付宝 openId） */
    private String customerOpenId;

    /** 订单商品总数量 */
    private Integer totalQuantity;

    /** 支付方式 */
    private PaymentMethodEnum paymentMethod;
}
