package cn.dextea.trade.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlatformEnum {

    @JsonProperty("weixin")
    WEIXIN("weixin", PayMethodEnum.WECHAT),

    @JsonProperty("alipay")
    ALIPAY("alipay", PayMethodEnum.ALIPAY);

    private final String value;
    private final PayMethodEnum payMethod;
}
