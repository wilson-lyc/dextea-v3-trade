package cn.dextea.trade.infrastructure.acl.store;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StoreQueryImpl implements StoreQuery {

    private final StoreMapper storeMapper;

    @Override
    public StorePO findById(Long id) {
        if (id == null) {
            return null;
        }
        return storeMapper.selectById(id);
    }

    @Override
    public List<StorePO> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return storeMapper.selectByIds(ids);
    }
}
