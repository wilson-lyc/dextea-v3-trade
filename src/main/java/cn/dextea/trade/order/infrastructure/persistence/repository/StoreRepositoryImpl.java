package cn.dextea.trade.order.infrastructure.persistence.repository;

import cn.dextea.trade.order.domain.model.Store;
import cn.dextea.trade.order.domain.repository.StoreRepository;
import cn.dextea.trade.order.infrastructure.persistence.converter.StoreConverter;
import cn.dextea.trade.order.infrastructure.persistence.mapper.StoreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepository {

    private final StoreMapper storeMapper;
    private final StoreConverter storeConverter;

    @Override
    public Store getStoreById(Long id) {
        return storeConverter.toDomain(storeMapper.selectById(id));
    }
}
