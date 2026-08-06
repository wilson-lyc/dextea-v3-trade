package cn.dextea.trade.order.infrastructure.persistence.repository;

import cn.dextea.trade.order.domain.model.Store;
import cn.dextea.trade.order.domain.repository.StoreRepository;
import cn.dextea.trade.order.infrastructure.persistence.converter.StoreConverter;
import cn.dextea.trade.order.infrastructure.persistence.mapper.StoreMapper;
import cn.dextea.trade.order.infrastructure.persistence.po.StorePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepository {

    private final StoreMapper storeMapper;
    private final StoreConverter storeConverter;

    @Override
    public Store getStoreById(Long id) {
        StorePO po = storeMapper.selectById(id);
        if (po == null) {
            return null;
        }
        return storeConverter.toDomain(po);
    }

    @Override
    public Map<Long, Store> getStoresByIds(Collection<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<StorePO> storePOs = storeMapper.selectByIds(storeIds);
        if (storePOs == null || storePOs.isEmpty()) {
            return Collections.emptyMap();
        }
        return storePOs.stream()
                .map(storeConverter::toDomain)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Store::getId, Function.identity(), (a, b) -> a));
    }
}
