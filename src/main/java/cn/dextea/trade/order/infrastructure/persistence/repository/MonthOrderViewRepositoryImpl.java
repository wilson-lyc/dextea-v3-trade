package cn.dextea.trade.order.infrastructure.persistence.repository;

import cn.dextea.trade.order.domain.port.MonthOrderViewRepository;
import cn.dextea.trade.order.infrastructure.persistence.mapper.OrderMapper;
import cn.dextea.trade.order.infrastructure.persistence.mapper.StoreMapper;
import cn.dextea.trade.order.infrastructure.persistence.po.OrderPO;
import cn.dextea.trade.order.infrastructure.persistence.po.StorePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MonthOrderViewRepositoryImpl implements MonthOrderViewRepository {

    private final OrderMapper orderMapper;
    private final StoreMapper storeMapper;

    @Override
    public Map<Long, String> findStoreNames(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, OrderPO> orderById = orderMapper.selectByIds(orderIds).stream()
                .collect(Collectors.toMap(OrderPO::getId, Function.identity(), (a, b) -> a));

        return orderIds.stream()
                .filter(orderById::containsKey)
                .collect(Collectors.toMap(Function.identity(),
                        orderId -> findStoreName(orderById.get(orderId).getStoreId()), (a, b) -> a));
    }

    private String findStoreName(Long storeId) {
        StorePO storePO = storeMapper.selectById(storeId);
        return storePO == null || storePO.getName() == null ? "未知门店" : storePO.getName();
    }
}
