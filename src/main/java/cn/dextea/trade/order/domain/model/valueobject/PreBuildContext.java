package cn.dextea.trade.order.domain.model.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 预构建（只读计价）上下文（领域值对象），由应用层由命令转换而来。
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreBuildContext {

    private Long storeId;

    private Long customerId;

    private List<PreBuildProductInput> products;
}
