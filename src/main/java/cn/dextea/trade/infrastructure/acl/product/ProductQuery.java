package cn.dextea.trade.infrastructure.acl.product;

import java.util.List;

public interface ProductQuery {

    ProductPO findById(Long id);

    List<ProductPO> findByIds(List<Long> ids);

}
