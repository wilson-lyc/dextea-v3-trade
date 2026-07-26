package cn.dextea.trade.order.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "门店信息")
public class StoreInfo {

    @Schema(description = "门店ID", example = "1")
    private Long id;

    @Schema(description = "门店名称", example = "杭州西湖店")
    private String name;

    @Schema(description = "门店地址", example = "浙江省杭州市西湖区文一路100号")
    private String address;

    @Schema(description = "门店联系电话", example = "0571-88888888")
    private String phone;

    @Schema(description = "营业时间", example = "09:00-22:00")
    private String businessHours;
}
