package cn.dextea.trade.dto;

import com.alipay.v3.model.AlipayTradeCreateModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建支付宝交易请求")
public class CreateAlipayTradeRequest {

    /**
     * op_app_id 示例值，后续由 alipay 配置类从 Nacos 读取后替换。
     */
    private static final String OP_APP_ID_EXAMPLE = "1234567890";

    @NotBlank(message = "orderNo 不能为空")
    @Schema(description = "商户订单号，对应 out_trade_no", example = "20150320010101001")
    private String orderNo;

    @NotNull(message = "totalPrice 不能为空")
    @Schema(description = "订单总金额（元），对应 total_amount", example = "88.88")
    private BigDecimal totalPrice;

    @NotBlank(message = "subject 不能为空")
    @Schema(description = "订单标题，对应 subject", example = "Iphone6 16G")
    private String subject;

    @NotBlank(message = "customerAlipayOpenId 不能为空")
    @Schema(description = "买家支付宝用户唯一标识(openId)，对应 buyer_open_id",
            example = "074a1CcTG1LelxKe4xQC0zgNdId0nxi95b5lsNpazWYoCo5")
    private String customerAlipayOpenId;

    /**
     * 转换为支付宝 SDK 所需的 {@link AlipayTradeCreateModel}。
     * 仅填充必填字段，其余字段留空。
     */
    public AlipayTradeCreateModel toAlipayTradeCreateModel() {
        return new AlipayTradeCreateModel()
                .outTradeNo(orderNo)
                .totalAmount(totalPrice.toPlainString())
                .productCode("JSAPI_PAY")
                .subject(subject)
                .opAppId(OP_APP_ID_EXAMPLE)
                .buyerOpenId(customerAlipayOpenId);
    }
}
