package cn.dextea.trade.order.application.dto.result;

import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.model.OrderItem;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrderDetailResult {

    private Order order;

    private List<OrderItem> items;
}
