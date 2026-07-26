package cn.dextea.trade.order.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 门店信息视图（领域值对象）。
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreInfoView {

    private Long id;

    private String name;

    private String address;

    private String phone;

    private String businessHours;
}
