package cn.dextea.trade.infrastructure.acl.customizationoption;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomizationOptionQueryImpl implements CustomizationOptionQuery {

    private final CustomizationOptionMapper customizationOptionMapper;

    @Override
    public CustomizationOptionPO findById(Long id) {
        if (id == null) {
            return null;
        }
        return customizationOptionMapper.selectById(id);
    }

    @Override
    public List<CustomizationOptionPO> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return customizationOptionMapper.selectByIds(ids);
    }
}
