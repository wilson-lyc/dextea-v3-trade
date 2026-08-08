package cn.dextea.trade.order.application.dto.command;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
@NoArgsConstructor
public class GetMonthOrdersCommand {

    private Long customerId;

    private Integer year;

    private Integer month;

    private LocalDateTime startAt;

    private LocalDateTime endAt;
}
