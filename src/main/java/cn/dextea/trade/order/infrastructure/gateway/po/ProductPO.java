package cn.dextea.trade.order.infrastructure.gateway.po;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPO {
    private Long id;
    private String name;
    private Integer status;
    private BigDecimal price;
}
