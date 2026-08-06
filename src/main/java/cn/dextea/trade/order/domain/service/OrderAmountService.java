package cn.dextea.trade.order.domain.service;

import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.model.OrderItem;
import cn.dextea.trade.shared.model.Money;
import cn.dextea.trade.shared.model.Quantity;
import org.springframework.stereotype.Component;

@Component
public class OrderAmountService {

    public Money calculateTotalPrice(Order order) {
        Money total = Money.ZERO;
        for (OrderItem item : order.getItems()) {
            if (item.getAvailable() == null || !item.getAvailable()) {
                continue;
            }
            total = total.add(item.getTotalPrice());
        }
        return total;
    }

    public Quantity calculateTotalQuantity(Order order) {
        Quantity total = Quantity.ZERO;
        for (OrderItem item : order.getItems()) {
            if (item.getAvailable() == null || !item.getAvailable()) {
                continue;
            }
            if (item.getQuantity() == null) {
                continue;
            }
            total = total.add(item.getQuantity());
        }
        return total;
    }
}