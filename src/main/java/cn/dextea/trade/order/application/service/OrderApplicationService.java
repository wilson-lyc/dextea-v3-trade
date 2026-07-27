package cn.dextea.trade.order.application.service;

import cn.dextea.trade.order.application.command.CreateOrderCommand;
import cn.dextea.trade.order.application.command.PreBuildOrderCommand;
import cn.dextea.trade.order.application.dto.OrderCreateResult;
import cn.dextea.trade.order.domain.model.PreBuildResult;

/**
 * 订单应用服务（命令侧）：编排预构建与创建订单。
 */
public interface OrderApplicationService {

    PreBuildResult preBuildOrder(PreBuildOrderCommand command);

    OrderCreateResult createOrder(CreateOrderCommand command);
}
