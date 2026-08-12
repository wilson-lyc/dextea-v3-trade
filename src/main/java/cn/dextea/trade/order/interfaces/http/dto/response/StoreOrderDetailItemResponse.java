package cn.dextea.trade.order.interfaces.http.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "门店订单详情中的订单项")
public class StoreOrderDetailItemResponse {

    @Schema(description = "订单项ID", example = "1")
    private Long id;

    @Schema(description = "商品ID", example = "1001")
    private Long productId;

    @Schema(description = "商品名称", example = "生椰拿铁")
    private String productName;

    @Schema(description = "SKU编号", example = "1001#1_2")
    private String skuId;

    @Schema(description = "客制化（原始值，未做格式转换）", example = "温度_热-糖度_少糖")
    private String customization;

    @Schema(description = "商品封面图", example = "https://example.com/a.jpg")
    private String coverUrl;

    @Schema(description = "数量", example = "2")
    private Integer quantity;

    @Schema(description = "单价（元）", example = "18.00")
    private BigDecimal unitPrice;

    @Schema(description = "小计（元）", example = "36.00")
    private BigDecimal totalPrice;

    @Schema(description = "是否可售", example = "true")
    private Boolean available;
}
