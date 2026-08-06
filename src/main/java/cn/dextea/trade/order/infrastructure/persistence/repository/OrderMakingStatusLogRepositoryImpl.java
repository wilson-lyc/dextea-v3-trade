package cn.dextea.trade.order.infrastructure.persistence.repository;

import cn.dextea.trade.order.domain.model.OrderMakingStatusLog;
import cn.dextea.trade.order.domain.repository.OrderMakingStatusLogRepository;
import cn.dextea.trade.order.infrastructure.persistence.converter.OrderMakingStatusLogConverter;
import cn.dextea.trade.order.infrastructure.persistence.mapper.OrderMakingStatusLogMapper;
import cn.dextea.trade.order.infrastructure.persistence.po.OrderMakingStatusLogPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderMakingStatusLogRepositoryImpl implements OrderMakingStatusLogRepository {

    private final OrderMakingStatusLogMapper mapper;
    private final OrderMakingStatusLogConverter converter;

    @Override
    public OrderMakingStatusLog save(OrderMakingStatusLog log) {
        OrderMakingStatusLogPO po = converter.toPO(log);
        mapper.insert(po);
        log.assignId(po.getId());
        return log;
    }

    @Override
    public List<OrderMakingStatusLog> saveAll(List<OrderMakingStatusLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return List.of();
        }
        List<OrderMakingStatusLogPO> pos = logs.stream().map(converter::toPO).toList();
        mapper.batchInsert(pos);
        for (int i = 0; i < logs.size(); i++) {
            logs.get(i).assignId(pos.get(i).getId());
        }
        return logs;
    }

    @Override
    public List<OrderMakingStatusLog> listByOrderId(Long orderId) {
        return converter.toModels(mapper.selectByOrderId(String.valueOf(orderId)));
    }
}
