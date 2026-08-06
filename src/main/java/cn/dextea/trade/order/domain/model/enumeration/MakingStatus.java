package cn.dextea.trade.order.domain.model.enumeration;

import cn.dextea.trade.shared.enumeration.CodeEnum;
import cn.dextea.trade.shared.enumeration.EnumUtils;

public enum MakingStatus implements CodeEnum {
    PENDING(0, "待制作"),
    PREPARING(1, "制作中"),
    READY(2, "制作完成"),
    COLLECTED(3, "已取餐");

    private final int code;
    private final String description;

    MakingStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static MakingStatus of(Integer code) {
        return EnumUtils.of(MakingStatus.class, code);
    }
}
