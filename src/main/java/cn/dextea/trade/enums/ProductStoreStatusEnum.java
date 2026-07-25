package cn.dextea.trade.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductStoreStatusEnum {

    SOLD_OUT(0, "售罄"),
    AVAILABLE(1, "可售");

    private final int code;
    private final String description;

    public static ProductStoreStatusEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProductStoreStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知门店状态: " + code);
    }
}
