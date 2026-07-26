package cn.dextea.trade.infrastructure.acl.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductQueryImpl implements ProductQuery {

    private final ProductMapper productMapper;

    @Override
    public ProductPO findById(Long id) {
        if (id == null) {
            return null;
        }
        return productMapper.selectById(id);
    }

    @Override
    public List<ProductPO> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return productMapper.selectByIds(ids);
    }

}
