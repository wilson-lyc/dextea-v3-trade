package cn.dextea.trade.order.domain.enums;

import cn.dextea.trade.common.enums.CodeEnum;
import cn.dextea.trade.common.enums.EnumUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 客制化选项的「门店维度」状态。
 *
 * <p>与 {@link CustomizationOptionGlobalStatusEnum}（全局维度）区分：
 * 选项在某个门店下可能被单独禁用，该门店状态应比对本枚举而非全局枚举。
 * 取值约定与 {@link ProductStoreStatusEnum} 对齐（0=门店不可用，1=门店可用）。</p>
 */
@Getter
@RequiredArgsConstructor
public enum CustomizationOptionStoreStatusEnum implements CodeEnum {

    UNAVAILABLE(0, "门店不可用"),
    AVAILABLE(1, "门店可用");

    private final int code;
    private final String description;

    public static CustomizationOptionStoreStatusEnum of(Integer code) {
        return EnumUtils.of(CustomizationOptionStoreStatusEnum.class, code);
    }
}
