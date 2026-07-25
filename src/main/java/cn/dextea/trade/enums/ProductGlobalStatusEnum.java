package cn.dextea.trade.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductGlobalStatusEnum {

    OFF_SHELF(0, "下架"),
    ON_SHELF(1, "上架");

    private final int code;
    private final String description;

    public static ProductGlobalStatusEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProductGlobalStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知商品全局状态: " + code);
    }
}
