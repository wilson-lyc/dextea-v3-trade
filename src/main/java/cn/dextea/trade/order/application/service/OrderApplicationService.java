package cn.dextea.trade.order.application.service;
import cn.dextea.trade.order.application.dto.command.CreateOrderCommand;
import cn.dextea.trade.order.application.dto.command.PreBuildOrderCommand;
import cn.dextea.trade.order.application.dto.result.OrderCreateResult;
import cn.dextea.trade.order.application.dto.result.PreBuildOrderResult;
public interface OrderApplicationService {
    PreBuildOrderResult preBuildOrder(PreBuildOrderCommand command);
    OrderCreateResult createOrder(CreateOrderCommand command);
}
