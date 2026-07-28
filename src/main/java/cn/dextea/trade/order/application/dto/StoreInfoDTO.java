package cn.dextea.trade.order.application.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
