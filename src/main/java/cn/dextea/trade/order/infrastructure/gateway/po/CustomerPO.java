package cn.dextea.trade.order.infrastructure.gateway.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 顾客表（customers）持久化对象：仅基础设施层可见。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerPO {

    private Long id;

    private String name;

    private Integer status;

    private String alipayOpenId;

    private String weixinOpenId;
}
