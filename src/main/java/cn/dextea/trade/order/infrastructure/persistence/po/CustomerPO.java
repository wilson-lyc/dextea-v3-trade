package cn.dextea.trade.order.infrastructure.persistence.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
