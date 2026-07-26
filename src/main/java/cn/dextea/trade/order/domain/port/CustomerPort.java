package cn.dextea.trade.order.domain.port;

import cn.dextea.trade.catalog.domain.model.Customer;

/**
 * 顾客防腐端口（只读）。
 */
public interface CustomerPort {

    Customer findById(Long id);
}
