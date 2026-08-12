package cn.dextea.trade.order.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetStoreWindowOrdersCommand {

    private Long storeId;

    private Integer hours;
}
