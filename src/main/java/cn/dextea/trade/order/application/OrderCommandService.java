package cn.dextea.trade.order.application;

import cn.dextea.trade.order.application.command.CreateOrderCommand;
import cn.dextea.trade.order.application.command.PreBuildOrderCommand;
import cn.dextea.trade.order.domain.model.OrderCreateResult;
import cn.dextea.trade.order.domain.model.PreBuildResult;

/**
 * 订单命令应用服务：编排预构建与创建订单。
 */
public interface OrderCommandService {

    PreBuildResult preBuildOrder(PreBuildOrderCommand command);

    OrderCreateResult createOrder(CreateOrderCommand command);
}
