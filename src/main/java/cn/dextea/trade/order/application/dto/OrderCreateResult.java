package cn.dextea.trade.order.application.dto;
import cn.dextea.trade.order.domain.model.valueobject.PreBuildResult;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import java.time.LocalDateTime;
@Getter
@Builder
@Jacksonized
public class OrderCreateResult {
    private Long id;
    private String orderNo;
    private String tradeNo;
    private LocalDateTime payExpireAt;
    private PreBuildResult preBuild;
}
