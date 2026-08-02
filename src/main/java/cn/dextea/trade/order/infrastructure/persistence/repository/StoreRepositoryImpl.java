package cn.dextea.trade.order.infrastructure.persistence.repository;

import cn.dextea.trade.order.domain.exception.StoreNotFoundException;
import cn.dextea.trade.order.domain.model.Store;
import cn.dextea.trade.order.domain.repository.StoreRepository;
import cn.dextea.trade.order.infrastructure.persistence.converter.StoreConverter;
import cn.dextea.trade.order.infrastructure.persistence.mapper.StoreMapper;
import cn.dextea.trade.order.infrastructure.persistence.po.StorePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepository {

    private final StoreMapper storeMapper;
    private final StoreConverter storeConverter;

    @Override
    public Store getStoreById(Long id) {
        StorePO po = storeMapper.selectById(id);
        if (po == null) {
            throw new StoreNotFoundException(id);
        }
        return storeConverter.toDomain(po);
    }
}
