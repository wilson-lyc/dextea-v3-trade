package cn.dextea.trade.order.infrastructure.persistence.repository;

import cn.dextea.trade.order.domain.model.aggregate.Store;
import cn.dextea.trade.order.domain.port.StoreRepository;
import cn.dextea.trade.order.infrastructure.persistence.mapper.ProductMapper;
import cn.dextea.trade.order.infrastructure.persistence.translator.StoreTranslator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepository {

    private final ProductMapper productMapper;

    @Override
    public Store findStore(Long id) {
        if (id == null) {
            return null;
        }
        return StoreTranslator.toStore(productMapper.selectStoreById(id));
    }
}
