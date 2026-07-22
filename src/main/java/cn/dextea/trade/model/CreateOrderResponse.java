package cn.dextea.trade.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "创建订单响应")
public class CreateOrderResponse extends AbstractOrderResponse {

    @Schema(description = "订单ID", example = "1")
    private Long id;

    @Schema(description = "订单号", example = "20150320010101001")
    private String orderNo;

    @Schema(description = "交易号", example = "2015042321001004720200028594")
    private String tradeNo;
}
