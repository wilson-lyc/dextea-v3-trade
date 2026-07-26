package cn.dextea.trade.infrastructure.acl.store;

import java.util.List;

public interface StoreQuery {

    StorePO findById(Long id);

    List<StorePO> findByIds(List<Long> ids);
}
