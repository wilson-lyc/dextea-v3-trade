package cn.dextea.trade.order.application.dto.command;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MarkOrderTimeoutCommand {
    private final String orderNo;
    private final LocalDateTime paymentExpiredAt;
}
