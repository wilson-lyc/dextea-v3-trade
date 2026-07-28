package cn.dextea.trade.order.application.service;
import cn.dextea.trade.order.application.command.CreateOrderCommand;
import cn.dextea.trade.order.application.command.PreBuildOrderCommand;
import cn.dextea.trade.order.application.dto.OrderCreateResult;
import cn.dextea.trade.order.domain.model.valueobject.PreBuildResult;
public interface OrderApplicationService {
    PreBuildResult preBuildOrder(PreBuildOrderCommand command);
    OrderCreateResult createOrder(CreateOrderCommand command);
}
