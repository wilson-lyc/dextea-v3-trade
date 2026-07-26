package cn.dextea.trade.infrastructure.acl.customization;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomizationQueryImpl implements CustomizationQuery {

    private final CustomizationMapper customizationMapper;

    @Override
    public CustomizationPO findById(Long id) {
        if (id == null) {
            return null;
        }
        return customizationMapper.selectById(id);
    }

    @Override
    public List<CustomizationPO> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return customizationMapper.selectByIds(ids);
    }
}
