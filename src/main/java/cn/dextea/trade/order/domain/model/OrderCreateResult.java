package cn.dextea.trade.order.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 创建订单结果（领域值对象），供应用层映射为对外响应。
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateResult {

    private Long id;

    private String orderNo;

    private String tradeNo;

    private PreBuildResult preBuild;
}
