package cn.dextea.trade.order.infrastructure.persistence.po;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderItemPO {
    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    private String skuId;
    private String customization;
    private String coverUrl;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
