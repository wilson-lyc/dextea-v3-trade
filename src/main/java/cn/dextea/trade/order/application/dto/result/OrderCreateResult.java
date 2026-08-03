package cn.dextea.trade.order.application.dto.result;

import cn.dextea.trade.order.application.dto.shared.PreBuildOrderItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class OrderCreateResult extends AbstractCreateOrderResult<PreBuildOrderItem> {
    private Long id;

    private String orderNo;

    private String tradeNo;

    private LocalDateTime paymentExpiredAt;
}
