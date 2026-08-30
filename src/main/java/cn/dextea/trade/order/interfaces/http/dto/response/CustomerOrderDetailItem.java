package cn.dextea.trade.order.interfaces.http.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class CustomerOrderDetailItem {

    private Long id;

    private Long productId;

    private String productName;

    private String skuId;

    private String customization;

    private String coverUrl;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal totalPrice;

    private Boolean available;
}
