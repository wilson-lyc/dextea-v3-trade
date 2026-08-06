package cn.dextea.trade.order.infrastructure.persistence.repository;

import cn.dextea.trade.order.domain.model.OrderPaymentStatusLog;
import cn.dextea.trade.order.domain.repository.OrderPaymentStatusLogRepository;
import cn.dextea.trade.order.infrastructure.persistence.converter.OrderPaymentStatusLogConverter;
import cn.dextea.trade.order.infrastructure.persistence.mapper.OrderPaymentStatusLogMapper;
import cn.dextea.trade.order.infrastructure.persistence.po.OrderPaymentStatusLogPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderPaymentStatusLogRepositoryImpl implements OrderPaymentStatusLogRepository {

    private final OrderPaymentStatusLogMapper mapper;
    private final OrderPaymentStatusLogConverter converter;

    @Override
    public OrderPaymentStatusLog save(OrderPaymentStatusLog log) {
        OrderPaymentStatusLogPO po = converter.toPO(log);
        mapper.insert(po);
        log.assignId(po.getId());
        return log;
    }

    @Override
    public List<OrderPaymentStatusLog> saveAll(List<OrderPaymentStatusLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return List.of();
        }
        List<OrderPaymentStatusLogPO> pos = logs.stream().map(converter::toPO).toList();
        mapper.batchInsert(pos);
        for (int i = 0; i < logs.size(); i++) {
            logs.get(i).assignId(pos.get(i).getId());
        }
        return logs;
    }

    @Override
    public List<OrderPaymentStatusLog> listByOrderId(Long orderId) {
        return converter.toModels(mapper.selectByOrderId(String.valueOf(orderId)));
    }
}
