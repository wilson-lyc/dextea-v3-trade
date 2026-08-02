package cn.dextea.trade.order.application.dto.result;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import java.time.LocalDateTime;

import cn.dextea.trade.order.application.dto.result.PreBuildOrderResult;
@Getter
@Builder
@Jacksonized
public class OrderCreateResult {
    private Long id;
    private String orderNo;
    private String tradeNo;
    private LocalDateTime payExpireAt;
    private PreBuildOrderResult preBuild;
}
