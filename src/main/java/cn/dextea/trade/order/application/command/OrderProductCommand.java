package cn.dextea.trade.order.application.command;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderProductCommand {
    private String skuId;
    private Integer quantity;
}
