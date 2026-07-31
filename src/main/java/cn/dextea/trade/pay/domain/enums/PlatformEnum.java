package cn.dextea.trade.pay.domain.enums;
import cn.dextea.trade.shared.domain.enumeration.CodeEnum;
import cn.dextea.trade.shared.domain.enumeration.EnumUtils;
import cn.dextea.trade.shared.domain.enumeration.StringCodeEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
@Getter
@RequiredArgsConstructor
public enum PlatformEnum implements StringCodeEnum, CodeEnum {
    @JsonProperty("weixin")
    WEIXIN("weixin", 1, "微信支付"),
    @JsonProperty("alipay")
    ALIPAY("alipay", 2, "支付宝");
    private final String key;
    private final int code;
    private final String description;
    @Override
    public String getValue() {
        return key;
    }
    public static PlatformEnum of(String value) {
        return EnumUtils.of(PlatformEnum.class, value);
    }
    public static PlatformEnum of(Integer code) {
        return EnumUtils.of(PlatformEnum.class, code);
    }
}
