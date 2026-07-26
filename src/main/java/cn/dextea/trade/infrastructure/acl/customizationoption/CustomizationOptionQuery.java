package cn.dextea.trade.infrastructure.acl.customizationoption;

import java.util.List;

public interface CustomizationOptionQuery {

    CustomizationOptionPO findById(Long id);

    List<CustomizationOptionPO> findByIds(List<Long> ids);
}
