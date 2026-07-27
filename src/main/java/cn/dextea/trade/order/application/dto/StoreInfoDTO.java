package cn.dextea.trade.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 门店信息（应用层 DTO）。
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreInfoDTO {

    private Long id;

    private String name;

    private String address;

    private String phone;

    private String businessHours;
}
