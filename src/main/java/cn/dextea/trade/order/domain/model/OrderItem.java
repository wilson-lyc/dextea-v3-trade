package cn.dextea.trade.order.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    private Long id;

    private Long orderId;

    private Long productId;

    private String skuId;

    private String productName;

    private Long coverId;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal subtotal;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
