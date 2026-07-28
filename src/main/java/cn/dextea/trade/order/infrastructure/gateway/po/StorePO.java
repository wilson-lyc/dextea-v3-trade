package cn.dextea.trade.order.infrastructure.gateway.po;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorePO {
    private Long id;
    private String name;
    private Integer status;
    private String address;
    private String phone;
    private String businessHours;
}
