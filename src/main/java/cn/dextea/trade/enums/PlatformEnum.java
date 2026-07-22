package cn.dextea.trade.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 支付平台枚举，对应下单请求中的 {@code platform} 字段。
 *
 * <p>用于把前端传入的支付平台标识映射为订单的支付方式 {@link PayMethodEnum}。
 * JSON 序列化/反序列化使用小写标识（{@code weixin} / {@code alipay}）。</p>
 */
@Getter
@RequiredArgsConstructor
public enum PlatformEnum {

    /** 微信支付 */
    @JsonProperty("weixin")
    WEIXIN("weixin", PayMethodEnum.WECHAT),

    /** 支付宝 */
    @JsonProperty("alipay")
    ALIPAY("alipay", PayMethodEnum.ALIPAY);

    private final String value;
    private final PayMethodEnum payMethod;
}
