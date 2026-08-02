package cn.dextea.trade.order.infrastructure.persistence.repository;

import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.Store;
import cn.dextea.trade.order.domain.repository.StoreRepository;
import cn.dextea.trade.order.infrastructure.persistence.converter.StoreConverter;
import cn.dextea.trade.order.infrastructure.persistence.mapper.StoreMapper;
import cn.dextea.trade.order.infrastructure.persistence.po.StorePO;
import cn.dextea.trade.shared.domain.error.BizError;
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
            throw new BizError(OrderErrorCode.STORE_NOT_FOUND);
        }
        return storeConverter.toDomain(po);
    }
}
