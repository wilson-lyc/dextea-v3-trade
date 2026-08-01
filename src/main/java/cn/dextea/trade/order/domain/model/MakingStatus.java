package cn.dextea.trade.order.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Map;

@Getter
@EqualsAndHashCode
public final class MakingStatus {

    public static final MakingStatus PENDING = new MakingStatus(0, "待制作");
    public static final MakingStatus PREPARING = new MakingStatus(1, "制作中");
    public static final MakingStatus READY = new MakingStatus(2, "制作完成");
    public static final MakingStatus COLLECTED = new MakingStatus(3, "已取餐");

    private static final Map<Integer, MakingStatus> CACHE = Map.of(
            0, PENDING,
            1, PREPARING,
            2, READY,
            3, COLLECTED
    );

    private final int code;
    private final String description;

    private MakingStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static MakingStatus of(int code) {
        MakingStatus status = CACHE.get(code);
        if (status == null) {
            throw new IllegalArgumentException("非法的制作状态枚举值: " + code);
        }
        return status;
    }

    @Override
    public String toString() {
        return Integer.toString(code);
    }
}
