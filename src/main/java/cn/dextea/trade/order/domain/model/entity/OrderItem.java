package cn.dextea.trade.order.domain.model.entity;

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

    /**
     * 商品封面标识（对应库表 order_items.cover_id）。
     * <p>由基础设施层的商品网关在下单时清洗给出，订单仅持久化该标识，
     * 查询时再经 {@code ProductGateway.findCoverUrls} 还原为展示 URL。
     * 领域层视其为不透明标识，不关心其背后的图库表结构。</p>
     */
    private Long coverId;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal subtotal;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
